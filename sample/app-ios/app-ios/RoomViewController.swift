//
//  RoomViewController.swift
//  app-ios
//
//  Created by Aleksandr Shepeliev on 03.08.2022.
//

import UIKit
import WebRTC
import shared

class RoomViewController: UIViewController {

    private let room: Room! = (UIApplication.shared.delegate as! AppDelegate).room
    
#if arch(x86_64)
    private var localVideo = RTCEAGLVideoView()
#else
    private var localVideo = RTCMTLVideoView()
#endif
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupLocalVideo()
        setupLocalVideoConstraints()

        room.model.subscribe(observer: roomModelObserver)
        room.openUserMedia()
    }
    
    private func roomModelObserver(_ model: RoomModel) {
        if let localStream = model.localStream {
            localStream.videoTracks.first?.addRenderer(renderer: localVideo)
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        room.hangup()
        room.model.unsubscribe(observer: roomModelObserver)
    }
    
    // MARK: - UI setup
    private func setupLocalVideo() {
        view.insertSubview(localVideo, at: 0)
    }
    
    private func setupLocalVideoConstraints() {
        localVideo.translatesAutoresizingMaskIntoConstraints = false
        localVideo.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        localVideo.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        localVideo.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        localVideo.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
