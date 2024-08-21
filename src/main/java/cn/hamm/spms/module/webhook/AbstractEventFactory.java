package cn.hamm.spms.module.webhook;

import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.webhook.enums.WebHookScene;
import cn.hamm.spms.module.webhook.enums.WebHookType;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <h1>抽象事件工厂</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Data
@Accessors(chain = true)
public abstract class AbstractEventFactory<E> {
    /**
     * <h2>当前数据</h2>
     */
    private E data;

    /**
     * <h2>场景</h2>
     */
    @Getter(AccessLevel.PRIVATE)
    private WebHookScene scene;

    /**
     * <h2>发起请求</h2>
     */
    public final void request() {
        DictionaryUtil dictionaryUtil = Utils.getDictionaryUtil();

        // 查询指定场景的Hook列表
        List<WebHookEntity> notifyHookList = Services.getWebHookService().filter(
                new WebHookEntity()
                        .setScene(scene.getKey())
        );
        notifyHookList.forEach(notifyHook -> {
            // 获取通知类型
            WebHookType webHookType = dictionaryUtil.getDictionary(WebHookType.class, notifyHook.getType());

            // 获取各个类型的通知内容(POST结构)
            Object object = switch (webHookType) {
                case WORK_WECHAT -> getWorkWechatMarkDown(notifyHook);
                case FEI_SHU -> getFeishuMarkDown(notifyHook);
                case DING_TALK -> getDingTalkMarkDown(notifyHook);
                case EMAIL -> getEmailBody(notifyHook);
                case WEB_HOOK -> getWebHookBody(notifyHook);
            };

            // 发起通知
            doRequest(object, notifyHook);
        });
    }

    /**
     * <h2>获取通知内容</h2>
     *
     * @param webHook 通知钩子
     * @return 准备的数据
     */
    protected abstract String getWebHookContent(WebHookEntity webHook);

    /**
     * <h2>请求</h2>
     *
     * @param data       数据
     * @param webHook 通知钩子
     */
    private void doRequest(@NotNull Object data, @NotNull WebHookEntity webHook) {
        DictionaryUtil dictionaryUtil = Utils.getDictionaryUtil();
        WebHookType webHookType = dictionaryUtil.getDictionary(WebHookType.class, webHook.getType());
        if (webHookType == WebHookType.EMAIL) {
            // 如果是邮箱通知 直接发送邮件
            try {
                Utils.getEmailUtil().sendEmail(webHook.getUrl(), scene.getLabel(), data.toString());
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        // 其他通知 发起网络请求
        Utils.getHttpUtil().setUrl(webHook.getUrl()).post(data.toString());
    }

    /**
     * <h2>获取企业微信MarkDown格式</h2>
     *
     * @param webHook 通知钩子
     * @return 企业微信MarkDown
     */
    protected final String getWorkWechatMarkDown(WebHookEntity webHook) {
        return Json.toString(Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "content", String.format("# %s\n\n%s", scene.getLabel(), getWebHookContent(webHook))
                )
        ));
    }

    /**
     * <h2>获取钉钉MarkDown格式</h2>
     *
     * @param webHook 通知钩子
     * @return 钉钉MarkDown
     */
    protected final String getDingTalkMarkDown(WebHookEntity webHook) {
        return Json.toString(Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "text", String.format("# %s\n\n%s", scene.getLabel(), getWebHookContent(webHook)),
                        "title", scene.getLabel()
                )
        ));
    }

    /**
     * <h2>获取飞书MarkDown格式</h2>
     *
     * @param webHook 通知钩子
     * @return 飞书MarkDown
     */
    protected final String getFeishuMarkDown(WebHookEntity webHook) {
        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(Map.of(
                "tag", "div",
                "text", Map.of(
                        "tag", "lark_md",
                        "content", String.format("# %s\n\n%s", scene.getLabel(), getWebHookContent(webHook))
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
                                "content", scene.getLabel()
                        )
                )
        ));
    }

    /**
     * <h2>获取邮件内容</h2>
     *
     * @param webHook 通知钩子
     * @return 邮件内容
     */
    protected final @NotNull String getEmailBody(WebHookEntity webHook) {
        return getWebHookContent(webHook).replaceAll("\n", "<br/>");
    }

    /**
     * <h2>获取WebHook请求包体</h2>
     *
     * @param webHook 通知钩子
     * @return WebHook内容
     */
    protected final String getWebHookBody(@NotNull WebHookEntity webHook) {
        return Json.toString(Map.of(
                "scene", scene.name(),
                "remark", webHook.getRemark(),
                "token", webHook.getToken(),
                "data", prepareWebHookData(webHook)));
    }

    /**
     * <h2>获取WebHook通知的数据</h2>
     *
     * @param webHook 通知钩子
     * @return 通知数据
     */
    protected Object prepareWebHookData(WebHookEntity webHook) {
        return data;
    }
}
