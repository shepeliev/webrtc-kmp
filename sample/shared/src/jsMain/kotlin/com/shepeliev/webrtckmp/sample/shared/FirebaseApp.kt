@file:JsModule("firebase/app")
@file:JsNonModule

package com.shepeliev.webrtckmp.sample.shared

import kotlin.js.Json

external class FirebaseApp

external fun initializeApp(config: Json): FirebaseApp
