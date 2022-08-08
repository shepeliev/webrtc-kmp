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
    
    @IBOutlet weak var createRoomButton: UIButton!
    
    @IBOutlet weak var joinRoomButton: UIButton!

    @IBOutlet weak var roomIdContainer: UIStackView!
    
    @IBOutlet weak var roomIdLabel: UILabel!

#if arch(x86_64)
    private var localVideo = RTCEAGLVideoView()
    private var remoteVideo = RTCEAGLVideoView()
#else
    private var localVideo = RTCMTLVideoView()
    private var remoteVideo = RTCMTLVideoView()
#endif
    
    private var room: Room! = (UIApplication.shared.delegate as! AppDelegate).room
    private var isLocalVideoAttached = false
    private var isRemoteVideoAttached = false
    private var localVideoTopConstraint: NSLayoutConstraint!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupLocalVideo()

        room.model.subscribe(observer: roomModelObserver)
        room.openUserMedia()
    }
        
    private func roomModelObserver(_ model: RoomModel) {
        NSLog("Room model updated: \(model)")

        if let localStream = model.localStream {
            localStreamReady(localStream)
        }

        if let remoteStream = model.remoteStream {
            remoteStreamReady(remoteStream)
        }
        
        if let roomId = model.roomId {
            createRoomButton.isHidden = true
            joinRoomButton.isHidden = true
            roomIdContainer.isHidden = false
            roomIdLabel.text = roomId
        }
    }

    private func localStreamReady(_ localStream: MediaStream) {
        if !isLocalVideoAttached {
            isLocalVideoAttached = true
            localStream.videoTracks.first?.addRenderer(renderer: localVideo)
        }
    }

    private func remoteStreamReady(_ remoteStream: MediaStream) {
        if !isRemoteVideoAttached {
            isRemoteVideoAttached = true
            setupRemoteVideo()
            
            if let track = remoteStream.videoTracks.first {
                track.addRenderer(renderer: remoteVideo)
            }
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        room.hangup()
        room.model.unsubscribe(observer: roomModelObserver)
    }
    
    // MARK: Actions
    
    @IBAction func createRoomButtonDidClick(_ sender: Any) {
        room.createRoom()
    }
    
    @IBAction func joinRoomButtonDidClick(_ sender: Any) {
        let joinDialog = UIAlertController(title: "Join room", message: "Enter room ID", preferredStyle: .alert)
        let okAction = UIAlertAction(title: "OK", style: .default) { [weak self] _ in
            if let roomId = joinDialog.textFields?.first?.text {
                self?.room?.joinRoom(roomId: roomId)
            }
        }
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel)
        joinDialog.addTextField()
        joinDialog.addAction(okAction)
        joinDialog.addAction(cancelAction)
        present(joinDialog, animated: true)
    }
    
    @IBAction func copyRoomIdDidClick(_ sender: Any) {
        if let roomId = roomIdLabel.text {
            UIPasteboard.general.string = roomId
            NSLog("Room ID \(roomId) copied.")
        }
    }
        
    // MARK: - UI
    private func setupLocalVideo() {
        localVideo.contentMode = .scaleAspectFill
        view.insertSubview(localVideo, at: 0)
        localVideo.translatesAutoresizingMaskIntoConstraints = false
        setupLocalVideoConstraints()
    }

    private func setupLocalVideoConstraints() {
        localVideo.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        localVideo.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        localVideo.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true

        localVideoTopConstraint = localVideo.topAnchor.constraint(equalTo: view.topAnchor)
        localVideoTopConstraint.isActive = true
    }

    private func setupRemoteVideo() {
        view.insertSubview(remoteVideo, at: 0)
        remoteVideo.translatesAutoresizingMaskIntoConstraints = false
        setupRemoteVideoConstraints()
    }

    private func setupRemoteVideoConstraints() {
        localVideoTopConstraint.isActive = false

        remoteVideo.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        remoteVideo.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        remoteVideo.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        remoteVideo.heightAnchor.constraint(equalTo: localVideo.heightAnchor).isActive = true

        localVideoTopConstraint = localVideo.topAnchor.constraint(equalTo: remoteVideo.bottomAnchor)
        localVideoTopConstraint.isActive = true

        localVideo.layoutIfNeeded()
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
