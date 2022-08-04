//
//  AppDelegate.swift
//  app-ios
//
//  Created by Aleksandr Shepeliev on 02.08.2022.
//

import UIKit
import shared
import FirebaseCore

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var lifecycle: LifecycleRegistry!
    var room: Room!
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        FirebaseApp.configure()
        
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        let context = DefaultComponentContext(lifecycle: self.lifecycle)
        room = RoomComponent(componentContext: context)

        lifecycle.onCreate()
        lifecycle.onStart()
        lifecycle.onResume()

        return true
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        lifecycle.onPause()
        lifecycle.onStop()
        lifecycle.onDestroy()
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}

