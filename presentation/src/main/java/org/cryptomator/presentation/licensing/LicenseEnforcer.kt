package org.cryptomator.presentation.licensing

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import org.cryptomator.presentation.R
import org.cryptomator.presentation.intent.Intents
import org.cryptomator.presentation.model.VaultModel
import org.cryptomator.presentation.presenter.ContextHolder
import org.cryptomator.util.FlavorConfig
import org.cryptomator.util.SharedPreferencesHandler
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LicenseEnforcer @Inject constructor(private val sharedPreferencesHandler: SharedPreferencesHandler) {

	enum class LockedAction(
		@StringRes val headerMessageRes: Int
	) {
		CREATE_VAULT(R.string.screen_license_check_locked_create_vault),
		UPLOAD_FILES(R.string.screen_license_check_locked_upload_files),
		CREATE_FOLDER(R.string.screen_license_check_locked_create_folder),
		CREATE_TEXT_FILE(R.string.screen_license_check_locked_create_text_file),
		SHARE_NODE(R.string.screen_license_check_locked_share_node),
		RENAME_NODE(R.string.screen_license_check_locked_rename_node),
		MOVE_NODE(R.string.screen_license_check_locked_move_node),
		DELETE_NODE(R.string.screen_license_check_locked_delete_node);

		companion object {
			fun fromName(name: String?): LockedAction? {
				return entries.firstOrNull { it.name == name }
			}
		}
	}

	fun hasWriteAccess(): Boolean {
		return true
	}

	fun hasPaidLicense() = false

	fun startTrial() {
		// No trial is required; all app features are available.
	}

	fun hasActiveTrial(): Boolean = false

	fun evaluateTrialState(): TrialState {
		return TrialState(isActive = false, isExpired = false, formattedExpirationDate = null)
	}

	private fun readTrialState(): TrialState {
		val trialExpiration = sharedPreferencesHandler.trialExpirationDate()
		val now = System.currentTimeMillis()
		val sticky = sharedPreferencesHandler.isTrialExpired()
		val active = isTrialActive(trialExpiration, now, sticky)
		val expired = trialExpiration > 0 && (trialExpiration <= now || sticky)
		val formattedDate = if (active || expired) {
			DateFormat.getDateInstance().format(Date(trialExpiration))
		} else null
		return TrialState(active, expired, formattedDate)
	}

	private fun isTrialActive(trialExpiration: Long, now: Long, sticky: Boolean): Boolean {
		return trialExpiration > 0 && trialExpiration > now && !sticky
	}

	data class TrialState(val isActive: Boolean, val isExpired: Boolean, val formattedExpirationDate: String?)

	data class LicenseUiState(
		val hasWriteAccess: Boolean,
		val hasPaidLicense: Boolean,
		val hasLifetimeLicense: Boolean,
		val hasRunningSubscription: Boolean,
		val trialState: TrialState
	)

	fun evaluateUiState(): LicenseUiState {
		val trialState = evaluateTrialState()
		val paidLicense = hasPaidLicense()
		return LicenseUiState(
			hasWriteAccess = true,
			hasPaidLicense = paidLicense,
			hasLifetimeLicense = sharedPreferencesHandler.licenseToken().isNotEmpty(),
			hasRunningSubscription = sharedPreferencesHandler.hasRunningSubscription(),
			trialState = trialState
		)
	}

	fun ensureWriteAccess(activity: Activity, action: LockedAction): Boolean {
		return true
	}

	fun hasWriteAccessForVault(vault: VaultModel?): Boolean {
		return true
	}

	/**
	 * Like [hasWriteAccessForVault], but doesn't record that the trial expired, see [evaluateTrialState], so that the app
	 * still tells the user once it is opened. For callers that don't show the license state, e.g. the documents provider.
	 */
	fun peekWriteAccessForVault(vault: VaultModel?): Boolean {
		return true
	}

	fun ensureWriteAccessForVault(activity: Activity, vault: VaultModel?, action: LockedAction): Boolean {
		return true
	}

}
