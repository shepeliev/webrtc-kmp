//
//  ViewController.swift
//  iosAppSwift
//
//  Created by Aleksandr Shepeliev on 09.03.2021.
//

import UIKit
import shared
import WebRTC

class WelcomeViewController: UIViewController {

    @IBOutlet weak var videoView: RTCMTLVideoView!
    
    private let mediaDevices = MediaDevices()
    private var stream: MediaStream?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let f = PeerConnectionFactory.Companion()
        f.build(options: nil)
    }
    
    @IBAction func startVideoPressed(_ sender: UIButton) {
        NSLog("Trying to start video...")
        mediaDevices.getUserMedia(audio: false, video: true) { stream, error in
            if let err = error {
                print("Error: \(err.localizedDescription)")
                return
            }
            
            if let currentStream = self.stream {
                print("Stop current video strteam")
                currentStream.videoTracks.forEach {
                    $0.native.remove(self.videoView)
                    $0.stop()
                }
            }
            
            self.stream = stream
            stream?.videoTrack()?.native.add(self.videoView)
            print("Video started \(String(describing: stream?.id))")
        }
    }
    
    @IBAction func stopVideoPressed(_ sender: UIButton) {
        print("Stop video")
        stream?.videoTracks.forEach {
            $0.native.remove(videoView)
            $0.stop()
        }
    }
    
    @IBAction func enumeratePressed(_ sender: UIButton) {
        mediaDevices.enumerateDevices { devices, error in
            if let err = error {
                print("Error: \(err.localizedDescription)")
                return
            }
            
            devices?.forEach { print("\($0)")}
        }
    }
    
    @IBAction func switchPressed(_ sender: UIButton) {
        mediaDevices.switchCamera { (res, err) in
            if let description = err?.localizedDescription {
                print("Error: \(description)")
                return
            }

            print("Camera switched (isFront = \(String(describing: res?.isFrontFacing))")
        }        
    }
}


