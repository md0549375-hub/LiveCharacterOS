package com.killer.livecharacter

import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.model.RightsRecord
import com.killer.livecharacter.model.RightsStatus
import com.killer.livecharacter.rights.RightsGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RightsGateTest {
    @Test fun originalCharacterLoads() = assertTrue(RightsGate.canLoad(CharacterProfile("original", "Original", rights = RightsRecord(status = RightsStatus.ORIGINAL))))
    @Test fun licensedCharacterRequiresLicenseId() = assertFalse(RightsGate.canLoad(CharacterProfile("licensed", "Licensed", rights = RightsRecord(status = RightsStatus.LICENSED))))
    @Test fun licensedCharacterWithLicenseIdLoads() = assertTrue(RightsGate.canLoad(CharacterProfile("licensed", "Licensed", rights = RightsRecord(status = RightsStatus.LICENSED, licenseId = "TEST-001"))))
    @Test fun unknownCharacterDoesNotLoad() = assertFalse(RightsGate.canLoad(CharacterProfile("unknown", "Unknown", rights = RightsRecord(status = RightsStatus.UNKNOWN))))
    @Test fun restrictedCharacterDoesNotLoad() = assertFalse(RightsGate.canLoad(CharacterProfile("restricted", "Restricted", rights = RightsRecord(status = RightsStatus.RESTRICTED))))
    @Test fun permittedUseRequiresPermissionNote() {
        val missing = CharacterProfile("permitted", "Permitted", rights = RightsRecord(status = RightsStatus.PERMITTED_USE))
        val present = CharacterProfile("permitted", "Permitted", rights = RightsRecord(status = RightsStatus.PERMITTED_USE, permissionNote = "Written permission record"))
        assertFalse(RightsGate.canLoad(missing))
        assertTrue(RightsGate.canLoad(present))
    }
}
