package org.cryptomator.presentation.licensing

import org.cryptomator.presentation.model.VaultModel
import org.cryptomator.util.SharedPreferencesHandler
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class LicenseEnforcerTest {

	private lateinit var sharedPreferencesHandler: SharedPreferencesHandler
	private lateinit var licenseEnforcer: LicenseEnforcer

	@BeforeEach
	fun setUp() {
		sharedPreferencesHandler = mock()
		licenseEnforcer = LicenseEnforcer(sharedPreferencesHandler)
	}

	@Test
	fun `all local writes are allowed without a license`() {
		assertTrue(licenseEnforcer.hasWriteAccess())
		assertFalse(licenseEnforcer.hasPaidLicense())
		assertFalse(licenseEnforcer.hasActiveTrial())
	}

	@Test
	fun `trial state is always free and inactive`() {
		val state = licenseEnforcer.evaluateTrialState()
		assertFalse(state.isActive)
		assertFalse(state.isExpired)
	}

	@Test
	fun `all vault writes are allowed`() {
		assertTrue(licenseEnforcer.hasWriteAccessForVault(null))
		assertTrue(licenseEnforcer.hasWriteAccessForVault(mock<VaultModel>()))
		assertTrue(licenseEnforcer.peekWriteAccessForVault(mock<VaultModel>()))
	}

	@Test
	fun `write guards never block`() {
		assertTrue(licenseEnforcer.ensureWriteAccess(mock(), LicenseEnforcer.LockedAction.UPLOAD_FILES))
		assertTrue(
			licenseEnforcer.ensureWriteAccessForVault(
				mock(),
				mock<VaultModel>(),
				LicenseEnforcer.LockedAction.UPLOAD_FILES
			)
		)
	}
}
