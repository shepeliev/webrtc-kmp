/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.shepeliev.apprtckmm

import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.SessionDescription

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
interface AppRtcClient {

    /**
     * Asynchronously connect to an AppRTC room URL using supplied connection
     * parameters. Once connection is established onConnectedToRoom()
     * callback with room parameters is invoked.
     */
    suspend fun connectToRoom(connectionParameters: RoomConnectionParameters): SignalingParameters

    /**
     * Send offer SDP to the other participant.
     */
    suspend fun sendOfferSdp(sdp: SessionDescription)

    /**
     * Send answer SDP to the other participant.
     */
    suspend fun sendAnswerSdp(sdp: SessionDescription)

    /**
     * Send Ice candidate to the other participant.
     */
    suspend fun sendLocalIceCandidate(candidate: IceCandidate)

    /**
     * Send removed ICE candidates to the other participant.
     */
    suspend fun sendLocalIceCandidateRemovals(candidates: List<IceCandidate>)

    /**
     * Disconnect from room.
     */
    suspend fun disconnectFromRoom()

    /**
     * Callback interface for messages delivered on signaling channel.
     *
     *
     * Methods are guaranteed to be invoked on the UI thread of |activity|.
     */
    interface SignalingEvents {
        /**
         * Callback fired once remote SDP is received.
         */
        suspend fun onRemoteDescription(sdp: SessionDescription)

        /**
         * Callback fired once remote Ice candidate is received.
         */
        suspend fun onRemoteIceCandidate(candidate: IceCandidate)

        /**
         * Callback fired once remote Ice candidate removals are received.
         */
        suspend fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>)

        /**
         * Callback fired once channel is closed.
         */
        suspend fun onChannelClose()

        /**
         * Callback fired once channel error happened.
         */
        suspend fun onChannelError(description: String?)
    }
}
