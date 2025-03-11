package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.helper.AirHelper;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.airpower.util.HttpUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.open.notify.enums.NotifyChannel;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class NotifyService extends BaseService<NotifyEntity, NotifyRepository> {
    /**
     * <h3>线程池</h3>
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            5,
            20,
            3600L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    /**
     * <h3>发送通知</h3>
     *
     * @param notifyScene 通知场景
     * @param data        通知数据
     * @param content     通知文案
     */
    public <T> void sendNotification(NotifyScene notifyScene, T data, String content) {
        try {
            EXECUTOR.submit(() -> {
                // 查询指定场景的Hook列表
                List<NotifyEntity> notifyList = Services.getNotifyService().filter(
                        new NotifyEntity()
                                .setScene(notifyScene.getKey())
                );
                final String title = notifyScene.getLabel();
                notifyList.forEach(notify -> {
                    // 获取通知类型
                    NotifyChannel notifyChannel = DictionaryUtil.getDictionary(NotifyChannel.class, notify.getChannel());

                    // 获取各个类型的通知内容(POST结构)
                    String requestData = switch (notifyChannel) {
                        case WORK_WECHAT -> getWorkWechatMarkDown(title, content);
                        case FEI_SHU -> getFeishuMarkDown(title, content);
                        case DING_TALK -> getDingTalkMarkDown(title, content);
                        case EMAIL -> getEmailBody(content);
                        case WEB_HOOK -> getNotifyWebHookBody(notify, data);
                    };

                    // 发起通知
                    doRequest(notify, requestData);
                });
            });
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * <h3>请求</h3>
     *
     * @param notify 通知
     * @param data   数据
     */
    private <T> void doRequest(@NotNull NotifyEntity notify, @NotNull T data) {
        NotifyChannel notifyChannel = DictionaryUtil.getDictionary(NotifyChannel.class, notify.getChannel());
        if (notifyChannel == NotifyChannel.EMAIL) {
            // 如果是邮箱通知 直接发送邮件
            try {
                NotifyScene scene = DictionaryUtil.getDictionary(NotifyScene.class, notify.getScene());
                AirHelper.getEmailHelper().sendEmail(notify.getUrl(), scene.getLabel(), data.toString());
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        // 其他通知 发起网络请求
        HttpUtil.create().setUrl(notify.getUrl()).post(data.toString());
    }

    /**
     * <h3>获取企业微信MarkDown格式</h3>
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 企业微信MarkDown
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
     * <h3>获取钉钉MarkDown格式</h3>
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 钉钉MarkDown
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
     * <h3>获取飞书MarkDown格式</h3>
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 飞书MarkDown
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
     * <h3>获取邮件内容</h3>
     *
     * @param content 通知内容
     * @return 邮件内容
     */
    @Contract(pure = true)
    protected final @NotNull String getEmailBody(@NotNull String content) {
        return content.replaceAll("\n", "<br/>");
    }

    /**
     * <h3>获取通知WebHook包体</h3>
     *
     * @param notify 通知
     * @return 通知包体
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
