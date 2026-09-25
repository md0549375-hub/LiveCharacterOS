package com.killer.livecharacter.rights

import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.model.RightsRecord
import com.killer.livecharacter.model.RightsSource
import com.killer.livecharacter.model.RightsStatus

object CharacterCatalog {
    fun defaults(): List<CharacterProfile> = listOf(
        CharacterProfile("astra", "Astra", rights = RightsRecord(
            status = RightsStatus.ORIGINAL, characterName = "Astra",
            sources = listOf(RightsSource("character", "LiveCharacterOS internal original", note = "Uygulama içi özgün demo karakteri."))
        )),
        CharacterProfile("licensed-demo", "Licensed Demo", rights = RightsRecord(
            status = RightsStatus.LICENSED, characterName = "Licensed Demo",
            company = "Example Rights Holder", workTitle = "Example Work", licenseId = "DEMO-LICENSE-001",
            sources = listOf(RightsSource("license", "Example license record", note = "Yalnızca sistem testi için örnek kayıt; gerçek lisans belgesi değildir."))
        )),
        CharacterProfile("unknown-demo", "Unknown Demo", rights = RightsRecord(
            status = RightsStatus.UNKNOWN, characterName = "Unknown Demo",
            sources = listOf(RightsSource("source", "Kaynak tanımlanmamış", note = "Rights Gate davranışını test etmek için örnek kayıt."))
        )),
        CharacterProfile("restricted-demo", "Restricted Demo", rights = RightsRecord(
            status = RightsStatus.RESTRICTED, characterName = "Restricted Demo",
            sources = listOf(RightsSource("restriction", "Uygulama erişim kısıtı", note = "Rights Gate davranışını test etmek için örnek kayıt."))
        ))
    )
    fun find(id: String): CharacterProfile? = defaults().firstOrNull { it.id == id }
}
