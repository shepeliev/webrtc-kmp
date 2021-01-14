//
//  main.m
//  AppRTCKMM
//
//  Created by Aleksandr Shepeliev on 12.01.2021.
//

#import <UIKit/UIKit.h>
#import "ARDAppDelegate.h"

//int main(int argc, char * argv[]) {
//    NSString * appDelegateClassName;
//    @autoreleasepool {
//        // Setup code that might create autoreleased objects goes here.
//        appDelegateClassName = NSStringFromClass([AppDelegate class]);
//    }
//    return UIApplicationMain(argc, argv, nil, appDelegateClassName);
//}

int main(int argc, char* argv[]) {
  @autoreleasepool {
    return UIApplicationMain(
        argc, argv, nil, NSStringFromClass([ARDAppDelegate class]));
  }
}
