package dev.kuro9.internal.smartapp.webhook.handler

import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookResponse

/**
 * webhook 사용 시 해당 인터페이스 타입을 가진 컴포넌트 빈으로 등록
 */
interface SmartAppWebhookHandler {

    suspend fun onVerifyApp(appId: String, verificationUrl: String)
    suspend fun onInitializePhase(
        appId: String,
        pageId: String,
        prevPageId: String?,
    ): SmartAppWebhookResponse.ConfigurationData.InitData

    suspend fun onPagePhase(
        appId: String,
        pageId: String,
        prevPageId: String?,
    ): SmartAppWebhookResponse.ConfigurationData.PageData
}