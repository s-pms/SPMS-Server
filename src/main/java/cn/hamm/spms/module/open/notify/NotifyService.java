package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.HttpUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.StringUtil;
import cn.hamm.airpower.email.helper.EmailHelper;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.open.notify.enums.NotifyChannel;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <h1>通知钩子</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class NotifyService extends BaseService<NotifyEntity, NotifyRepository> {
    /**
     * 收件地址格式
     */
    private static final Pattern EMAIL_ADDRESS =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * IPv4 字面量
     */
    private static final Pattern LITERAL_IP = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    /**
     * 通知发送线程池，核心 5 / 最大 20 线程
     * <p>
     * 队列必须有界：{@code LinkedBlockingQueue} 无参构造的容量是
     * {@code Integer.MAX_VALUE}，堆积时不会拒绝任务，只会让任务无限堆积直至 OOM。
     * </p>
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            5,
            20,
            3600L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.AbortPolicy()
    );

    @Autowired
    private EmailHelper emailHelper;

    /**
     * 按场景向所有启用的通知钩子推送消息
     *
     * @param notifyScene 通知场景
     * @param data        通知数据
     * @param content     通知文案
     * @param <T>         通知数据类型
     * @apiNote 异步投递，调用方拿不到结果，异常只记日志不抛出。目标地址由
     * {@link #parseDeliverableUri} 限定协议并拒绝内网主机
     */
    public <T> void sendNotification(NotifyScene notifyScene, T data, String content) {
        try {
            EXECUTOR.submit(() -> {
                List<NotifyEntity> notifyList = filter(
                        new NotifyEntity()
                                .setScene(notifyScene.getKey())
                                .setIsDisabled(false)
                );
                final String title = notifyScene.getLabel();
                notifyList.forEach(notify -> {
                    NotifyChannel notifyChannel = DictionaryUtil.getDictionary(NotifyChannel.class, notify.getChannel());

                    String requestData = switch (notifyChannel) {
                        case WORK_WECHAT -> getWorkWechatMarkDown(title, content);
                        case FEI_SHU -> getFeishuMarkDown(title, content);
                        case DING_TALK -> getDingTalkMarkDown(title, content);
                        case EMAIL -> getEmailBody(content);
                        case WEB_HOOK -> getNotifyWebHookBody(notify, data);
                    };

                    doRequest(notify, requestData);
                });
            });
        } catch (RejectedExecutionException e) {
            // 队列已满：丢弃本次通知而不是阻塞业务线程
            log.warn("通知队列已满，场景 {} 的本次通知被丢弃", notifyScene.getLabel());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 按渠道投递通知
     *
     * @param notify 通知
     * @param data   通知包体
     * @param <T>    通知包体类型
     */
    private <T> void doRequest(@NotNull NotifyEntity notify, @NotNull T data) {
        NotifyChannel notifyChannel = DictionaryUtil.getDictionary(NotifyChannel.class, notify.getChannel());
        if (notifyChannel == NotifyChannel.EMAIL) {
            if (!isDeliverableEmailAddress(notify.getUrl())) {
                log.warn("通知 {} 的收件地址不合法，已跳过投递", notify.getId());
                return;
            }
            try {
                NotifyScene scene = DictionaryUtil.getDictionary(NotifyScene.class, notify.getScene());
                emailHelper.sendEmail(notify.getUrl(), scene.getLabel(), data.toString());
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        URI target = parseDeliverableUri(notify.getUrl());
        if (Objects.isNull(target)) {
            log.warn("通知 {} 的目标地址不合法，已跳过投递", notify.getId());
            return;
        }
        HttpUtil.create().setUrl(target.toString()).post(data.toString());
    }

    /**
     * 解析可投递的 HTTP(S) 目标地址
     * <p>
     * 通知地址是管理员填写的任意字符串，直连会把内网服务与云元数据接口一并纳入
     * 访问范围，因此限定协议为 http/https，并拒绝指向回环、私有、链路本地与
     * 保留网段的主机名。
     * </p>
     *
     * @param raw 原始地址
     * @return 合法则返回 URI，否则返回 null
     */
    private @Nullable URI parseDeliverableUri(@Nullable String raw) {
        if (!StringUtil.hasText(raw)) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
        String scheme = Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return null;
        }
        if (!StringUtil.hasText(uri.getHost()) || isInternalHost(uri.getHost())) {
            return null;
        }
        return uri;
    }

    /**
     * 判断主机是否指向内网或保留网段
     *
     * @param host 主机名或字面量 IP
     * @return 是则返回 true
     * @apiNote 仅对字面量 IP 与已知本地名生效。主机名可能经 DNS 解析到内网，
     * 完整防护需要在建立连接前复核解析结果。
     */
    private boolean isInternalHost(@NotNull String host) {
        String target = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(target) || target.endsWith(".localhost") || target.endsWith(".local")) {
            return true;
        }
        if (!LITERAL_IP.matcher(target).matches()) {
            // 非字面量 IP，无法在不发起解析的情况下判断归属
            return false;
        }
        InetAddress address;
        try {
            address = InetAddress.getByName(target);
        } catch (UnknownHostException e) {
            return true;
        }
        return address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    /**
     * 判断是否为可投递的收件地址
     *
     * @param raw 原始地址
     * @return 合法则返回 true
     */
    private boolean isDeliverableEmailAddress(@Nullable String raw) {
        if (!StringUtil.hasText(raw)) {
            return false;
        }
        return EMAIL_ADDRESS.matcher(raw.trim()).matches();
    }

    /**
     * 获取企业微信 MarkDown 格式
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 企业微信 MarkDown
     */
    protected final String getWorkWechatMarkDown(String title, String content) {
        return Json.toString(Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "content", String.format("# %s\n\n%s", title, content)
                )
        ));
    }

    /**
     * 获取钉钉 MarkDown 格式
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 钉钉 MarkDown
     */
    protected final String getDingTalkMarkDown(String title, String content) {
        return Json.toString(Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "text", String.format("# %s\n\n%s", title, content),
                        "title", title
                )
        ));
    }

    /**
     * 获取飞书 MarkDown 格式
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 飞书 MarkDown
     */
    protected final String getFeishuMarkDown(String title, String content) {
        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(Map.of(
                "tag", "div",
                "text", Map.of(
                        "tag", "lark_md",
                        "content", String.format("# %s\n\n%s", title, content)
                )
        ));
        return Json.toString(Map.of(
                "msg_type", "interactive",
                "card", Map.of(
                        "elements", elements
                ),
                "header", Map.of(
                        "title", Map.of(
                                "tag", "plain_text",
                                "content", content
                        )
                )
        ));
    }

    /**
     * 获取邮件内容
     *
     * @param content 通知内容
     * @return 邮件内容
     */
    @Contract(pure = true)
    protected final @NotNull String getEmailBody(@NotNull String content) {
        return content.replace("\n", "<br/>");
    }

    /**
     * 构造 WebHook 通知包体
     *
     * @param notify 通知
     * @param data   通知数据
     * @param <T>    通知数据类型
     * @return 通知包体
     * @apiNote 令牌放在包体里而不是请求头，接收方需从 JSON 的 {@code token} 字段取
     */
    protected final <T> String getNotifyWebHookBody(@NotNull NotifyEntity notify, T data) {
        NotifyScene scene = DictionaryUtil.getDictionary(NotifyScene.class, notify.getScene());
        return Json.toString(Map.of(
                "scene", scene.name(),
                "remark", notify.getRemark(),
                "token", notify.getToken(),
                "data", data));
    }
}
