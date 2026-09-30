package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.HttpUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.email.helper.EmailHelper;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.open.notify.enums.NotifyChannel;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <h1>通知钩子</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class NotifyService extends BaseService<NotifyEntity, NotifyRepository> {
    /**
     * 通知发送线程池，核心 5 / 最大 20 线程，队列无界
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            5,
            20,
            3600L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
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
     * @apiNote 异步投递，调用方拿不到结果，异常只记日志不抛出。目标 URL 来自管理员配置，
     * 未做白名单校验，存在 SSRF 风险
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
            try {
                NotifyScene scene = DictionaryUtil.getDictionary(NotifyScene.class, notify.getScene());
                emailHelper.sendEmail(notify.getUrl(), scene.getLabel(), data.toString());
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        HttpUtil.create().setUrl(notify.getUrl()).post(data.toString());
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
