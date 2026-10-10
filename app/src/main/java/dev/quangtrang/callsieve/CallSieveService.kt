package dev.quangtrang.callsieve

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * The system starts this service on each incoming call; no background process needed.
 * It must respond quickly: just read rules from SharedPreferences and match the regex.
 */
class CallSieveService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        // On any error, let the call through; never block by mistake because of a crash.
        val block = try {
            val number = PhoneNumber.normalize(callDetails.handle?.schemeSpecificPart)
            RuleMatcher.firstMatch(number, RuleStore.load(this)) != null
        } catch (e: Exception) {
            false
        }

        val response = CallResponse.Builder().apply {
            if (block) {
                setDisallowCall(true)     // no ringing, no screen wake
                setRejectCall(true)       // reject the call
                setSkipNotification(true) // no missed-call notification
                setSkipCallLog(false)     // still log it for later review
            }
        }.build()

        respondToCall(callDetails, response)
    }
}
