package com.mrsep.musicrecognizer.core.metadata.tracklink.odesli

import kotlinx.serialization.Serializable

@Serializable
internal data class OdesliNextDataJson(
    val props: Props? = null,
) {
    @Serializable
    data class Props(
        val pageProps: PageProps? = null,
    ) {
        @Serializable
        data class PageProps(
            val pageData: PageData? = null,
        ) {
            @Serializable
            data class PageData(
                val sections: List<Section>? = null,
                val entityData: EntityData? = null,
            ) {
                @Serializable
                data class Section(
                    val links: List<Link>? = null,
                ) {
                    @Serializable
                    data class Link(
                        val platform: String? = null,
                        val url: String? = null,
                        val show: Boolean? = null,
                    )
                }
                @Serializable
                data class EntityData(
                    val thumbnailUrl: String? = null,
                    val thumbnailWidth: Int? = null,
                    val thumbnailHeight: Int? = null,
                )
            }
        }
    }
}