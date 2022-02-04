package com.shepeliev.webrtckmp.mediarecorder

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule

actual abstract class CameraPermissionGrantedTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)
}
