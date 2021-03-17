//
//  CallViewController.swift
//  iosAppSwift
//
//  Created by Aleksandr Shepeliev on 14.03.2021.
//

import UIKit
import shared
import WebRTC

class CallViewController: UIViewController {
            
    var roomUrl: String?
    var roomId: String?
    
    var callController: CallController?
    var iosView: IosView?
    
    private let lifecycle = LifecycleRegistry()
        
    @IBOutlet weak var foolScreenVideo: RTCEAGLVideoView!
    
    @IBOutlet weak var localVideoView: RTCCameraPreviewView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        iosView = IosView(self)
        callController = CallController(roomUrl: roomUrl!, roomId: roomId!, lifecycle: lifecycle)
        callController?.onViewCreated(view: iosView!, viewLifecycle: lifecycle)

        lifecycle.onCreate()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        lifecycle.onStart()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        lifecycle.onStop()
    }
    
    @IBAction func hangupPressed(_ sender: Any) {
        iosView?.dispatch(event: CallViewEvent.HangupClicked())
    }
}

class IosView: BaseMviView<CallViewModel, CallViewEvent>, CallView {
    
    private let viewController: CallViewController
    
    init(_ viewController: CallViewController) {
        self.viewController = viewController
        super.init()
    }
    
    func navigateBack() {
        viewController.dismiss(animated: true, completion: nil)
    }

    override func render(model: CallViewModel) {
        let renderer = viewController.localVideoView
        model.localVideoTrack?.addSink(renderer: RTCVideoRendererProtocolAdapter(native: renderer as! RTCVideoRenderer))
    }
}



