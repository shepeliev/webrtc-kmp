package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.externals.RTCPeerConnectionConfiguration

internal expect fun RtcConfiguration.toPlatform(): RTCPeerConnectionConfiguration
