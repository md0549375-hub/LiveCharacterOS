package com.killer.livecharacter.rights

import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.model.RightsStatus

data class RightsDecision(val allowed: Boolean, val title: String, val message: String)

object RightsGate {
    fun evaluate(profile: CharacterProfile): RightsDecision {
        val r = profile.rights
        return when (r.status) {
            RightsStatus.ORIGINAL -> RightsDecision(true, "Original karakter", "Karakter uygulama içinde özgün içerik olarak işaretli.")
            RightsStatus.LICENSED -> if (!r.licenseId.isNullOrBlank()) RightsDecision(true, "Lisans doğrulandı", "Kayıtlı lisans kimliği mevcut.") else RightsDecision(false, "Lisans kaydı eksik", "Licensed durumunda lisans kimliği olmadan karakter yüklenmez.")
            RightsStatus.USER_OWNED_LICENSE -> if (!r.licenseId.isNullOrBlank()) RightsDecision(true, "Kullanıcı lisansı doğrulandı", "Kullanıcıya ait lisans kaydı mevcut.") else RightsDecision(false, "Lisans kaydı eksik", "Kullanıcı lisansı belirtilmiş ancak lisans kimliği yok.")
            RightsStatus.PERMITTED_USE -> if (!r.permissionNote.isNullOrBlank()) RightsDecision(true, "İzinli kullanım", "Kullanım izni için kayıtlı açıklama mevcut.") else RightsDecision(false, "İzin kaydı eksik", "Permitted use durumunda izin kaydı olmadan karakter yüklenmez.")
            RightsStatus.RESTRICTED -> RightsDecision(false, "Karakter kısıtlandı", "Bu karakter için erişim engeli etkin. Karakter yüklenmeyecek.")
            RightsStatus.UNKNOWN -> RightsDecision(false, "Hak durumu bilinmiyor", "Kaynak veya kullanım hakkı doğrulanamadığı için karakter yüklenmeyecek.")
        }
    }
    fun canLoad(profile: CharacterProfile): Boolean = evaluate(profile).allowed
}
