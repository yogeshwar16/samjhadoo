import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export class WebRTCService {
  constructor() {
    this.stompClient = null;
    this.peerConnection = null;
    this.localStream = null;
    this.remoteStream = null;
    this.socketUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';
    this.isInitiator = false;
    this.iceServers = [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' },
    ];
    this.callbacks = {
      onLocalStream: null,
      onRemoteStream: null,
      onConnectionStateChange: null,
      onIceConnectionStateChange: null,
      onError: null,
    };
  }

  // Initialize the WebRTC connection
  async initialize(userId, sessionId, isInitiator = false) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.isInitiator = isInitiator;

    try {
      await this.setupMediaDevices();
      this.setupWebSocket();
      this.createPeerConnection();
      
      if (this.isInitiator) {
        this.createAndSendOffer();
      }
    } catch (error) {
      this.handleError('Error initializing WebRTC:', error);
    }
  }

  // Set up local media devices (camera and microphone)
  async setupMediaDevices() {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      });
      
      if (this.callbacks.onLocalStream) {
        this.callbacks.onLocalStream(this.localStream);
      }
    } catch (error) {
      this.handleError('Error accessing media devices:', error);
      throw error;
    }
  }

  // Set up WebSocket connection for signaling
  setupWebSocket() {
    const socket = new SockJS(this.socketUrl);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('WebSocket connected:', frame);
      
      // Subscribe to the user's private queue
      this.stompClient.subscribe(
        `/user/queue/signal`,
        this.handleSignalingMessage.bind(this)
      );
    };

    this.stompClient.onStompError = (frame) => {
      this.handleError('Broker reported error: ' + frame.headers['message']);
      this.handleError('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  // Create RTCPeerConnection
  createPeerConnection() {
    const configuration = {
      iceServers: this.iceServers,
      iceTransportPolicy: 'all',
    };

    this.peerConnection = new RTCPeerConnection(configuration);

    // Add local stream to peer connection
    this.localStream.getTracks().forEach(track => {
      this.peerConnection.addTrack(track, this.localStream);
    });

    // Set up event handlers
    this.peerConnection.onicecandidate = this.handleICECandidateEvent.bind(this);
    this.peerConnection.ontrack = this.handleTrackEvent.bind(this);
    this.peerConnection.oniceconnectionstatechange = this.handleICEConnectionStateChange.bind(this);
    this.peerConnection.onconnectionstatechange = this.handleConnectionStateChange.bind(this);
  }

  // Handle ICE candidate events
  handleICECandidateEvent(event) {
    if (event.candidate) {
      this.sendSignal({
        type: 'ice-candidate',
        candidate: event.candidate,
      });
    }
  }

  // Handle incoming tracks
  handleTrackEvent(event) {
    this.remoteStream = event.streams[0];
    if (this.callbacks.onRemoteStream) {
      this.callbacks.onRemoteStream(this.remoteStream);
    }
  }

  // Handle ICE connection state changes
  handleICEConnectionStateChange() {
    console.log('ICE connection state:', this.peerConnection.iceConnectionState);
    if (this.callbacks.onIceConnectionStateChange) {
      this.callbacks.onIceConnectionStateChange(this.peerConnection.iceConnectionState);
    }
  }

  // Handle connection state changes
  handleConnectionStateChange() {
    console.log('Connection state:', this.peerConnection.connectionState);
    if (this.callbacks.onConnectionStateChange) {
      this.callbacks.onConnectionStateChange(this.peerConnection.connectionState);
    }
  }

  // Create and send an offer
  async createAndSendOffer() {
    try {
      const offer = await this.peerConnection.createOffer({
        offerToReceiveAudio: true,
        offerToReceiveVideo: true,
      });
      
      await this.peerConnection.setLocalDescription(offer);
      
      this.sendSignal({
        type: 'offer',
        sdp: offer.sdp,
      });
    } catch (error) {
      this.handleError('Error creating offer:', error);
    }
  }

  // Handle incoming signaling messages
  async handleSignalingMessage(message) {
    const signal = JSON.parse(message.body);
    console.log('Received signal:', signal);

    try {
      switch (signal.type) {
        case 'offer':
          await this.handleOffer(signal);
          break;
        case 'answer':
          await this.handleAnswer(signal);
          break;
        case 'ice-candidate':
          await this.handleNewICECandidate(signal.candidate);
          break;
        default:
          console.warn('Unknown signal type:', signal.type);
      }
    } catch (error) {
      this.handleError('Error handling signal:', error);
    }
  }

  // Handle incoming offer
  async handleOffer(offer) {
    if (this.peerConnection.signalingState !== 'stable') {
      console.log('Signaling state is not stable, rolling back...');
      await Promise.all([
        this.peerConnection.setLocalDescription({ type: 'rollback' }),
        this.peerConnection.setRemoteDescription(offer),
      ]);
      return;
    }

    await this.peerConnection.setRemoteDescription(offer);
    const answer = await this.peerConnection.createAnswer();
    await this.peerConnection.setLocalDescription(answer);
    
    this.sendSignal({
      type: 'answer',
      sdp: answer.sdp,
    });
  }

  // Handle incoming answer
  async handleAnswer(answer) {
    await this.peerConnection.setRemoteDescription(answer);
  }

  // Handle new ICE candidate
  async handleNewICECandidate(candidate) {
    try {
      if (candidate) {
        await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
      }
    } catch (error) {
      if (error.name === 'TypeError') {
        console.warn('Failed to add ICE candidate:', error);
      } else {
        throw error;
      }
    }
  }

  // Send signaling message through WebSocket
  sendSignal(signal) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/signal/${this.sessionId}`,
        body: JSON.stringify({
          ...signal,
          senderId: this.userId,
          sessionId: this.sessionId,
        }),
      });
    } else {
      console.warn('WebSocket not connected, cannot send signal');
    }
  }

  // Clean up resources
  async cleanup() {
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }

    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }

  // Error handling
  handleError(message, error) {
    console.error(message, error);
    if (this.callbacks.onError) {
      this.callbacks.onError(message, error);
    }
  }

  // Set callback functions
  onLocalStream(callback) {
    this.callbacks.onLocalStream = callback;
    return this;
  }

  onRemoteStream(callback) {
    this.callbacks.onRemoteStream = callback;
    return this;
  }

  onConnectionStateChange(callback) {
    this.callbacks.onConnectionStateChange = callback;
    return this;
  }

  onIceConnectionStateChange(callback) {
    this.callbacks.onIceConnectionStateChange = callback;
    return this;
  }

  onError(callback) {
    this.callbacks.onError = callback;
    return this;
  }
}

export default new WebRTCService();
