//
//  ViewController.swift
//  iosAppSwift
//
//  Created by Aleksandr Shepeliev on 09.03.2021.
//

import UIKit
import shared
import WebRTC

class WelcomeViewController: UIViewController, LocalVideoListener {

    @IBOutlet weak var videoView: RTCMTLVideoView!
    
    private var localVideo: LocalVideo?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        localVideo = LocalVideo(listener: self)
        localVideo?.videoRenderer = RTCVideoRendererProtocolAdapter(native: videoView)
    }
    
    @IBAction func startVideoPressed(_ sender: UIButton) {
        NSLog("Trying to start video...")
        localVideo?.startVideo()
    }
    
    @IBAction func stopVideoPressed(_ sender: UIButton) {
        NSLog("Stop video")
        localVideo?.stopVideo()
    }
        
    @IBAction func switchPressed(_ sender: UIButton) {
        NSLog("Switch camera")
        localVideo?.switchCamera()
    }
    
    func onError(description: String?) {
        NSLog("Local video error: \(String(describing: description))")
    }
    
    func onVideoStarted() {
        NSLog("Video started")
    }
}


