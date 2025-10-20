import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import WebRTCService from '../../services/WebRTCService';
import { Box, Button, Grid, Paper, Typography, CircularProgress, IconButton } from '@mui/material';
import { Mic, MicOff, Videocam, VideocamOff, CallEnd, ScreenShare, StopScreenShare } from '@mui/icons-material';

const VideoCall = () => {
  const { sessionId } = useParams();
  const { user } = useAuth();
  
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoOff, setIsVideoOff] = useState(false);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [connectionState, setConnectionState] = useState('disconnected');
  const [iceConnectionState, setIceConnectionState] = useState('disconnected');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);
  const screenSharingStream = useRef(null);
  const isInitiator = useRef(false);

  useEffect(() => {
    // Determine if the current user is the initiator (mentor)
    // This could be determined from the session details or URL params
    isInitiator.current = true; // For demo purposes
    
    // Initialize WebRTC
    initializeWebRTC();
    
    // Clean up on unmount
    return () => {
      WebRTCService.cleanup();
      if (screenSharingStream.current) {
        screenSharingStream.current.getTracks().forEach(track => track.stop());
      }
    };
  }, [sessionId, user.id]);

  const initializeWebRTC = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Set up WebRTC service with callbacks
      WebRTCService
        .onLocalStream(handleLocalStream)
        .onRemoteStream(handleRemoteStream)
        .onConnectionStateChange(handleConnectionStateChange)
        .onIceConnectionStateChange(handleIceConnectionStateChange)
        .onError(handleError);
      
      // Initialize the WebRTC connection
      await WebRTCService.initialize(user.id, sessionId, isInitiator.current);
      
    } catch (err) {
      console.error('Error initializing WebRTC:', err);
      setError('Failed to initialize video call. Please check your camera and microphone permissions.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLocalStream = (stream) => {
    setLocalStream(stream);
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = stream;
    }
  };

  const handleRemoteStream = (stream) => {
    setRemoteStream(stream);
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = stream;
    }
  };

  const handleConnectionStateChange = (state) => {
    console.log('Connection state changed:', state);
    setConnectionState(state);
  };

  const handleIceConnectionStateChange = (state) => {
    console.log('ICE connection state changed:', state);
    setIceConnectionState(state);
  };

  const handleError = (message, error) => {
    console.error(message, error);
    setError(error?.message || 'An error occurred during the call');
  };

  const toggleMute = () => {
    if (localStream) {
      localStream.getAudioTracks().forEach(track => {
        track.enabled = !track.enabled;
      });
      setIsMuted(!isMuted);
    }
  };

  const toggleVideo = () => {
    if (localStream) {
      localStream.getVideoTracks().forEach(track => {
        track.enabled = !track.enabled;
      });
      setIsVideoOff(!isVideoOff);
    }
  };

  const toggleScreenShare = async () => {
    try {
      if (isScreenSharing) {
        // Stop screen sharing
        if (screenSharingStream.current) {
          screenSharingStream.current.getTracks().forEach(track => track.stop());
          screenSharingStream.current = null;
        }
        
        // Switch back to camera
        const stream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: true,
        });
        
        // Replace the tracks in the peer connection
        const videoTrack = stream.getVideoTracks()[0];
        const sender = WebRTCService.peerConnection
          .getSenders()
          .find(s => s.track && s.track.kind === 'video');
          
        if (sender) {
          await sender.replaceTrack(videoTrack);
        }
        
        // Update local stream
        if (localVideoRef.current) {
          localVideoRef.current.srcObject = stream;
        }
        
        setLocalStream(stream);
        setIsScreenSharing(false);
      } else {
        // Start screen sharing
        const stream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true,
        });
        
        screenSharingStream.current = stream;
        
        // Replace the video track in the peer connection
        const videoTrack = stream.getVideoTracks()[0];
        const sender = WebRTCService.peerConnection
          .getSenders()
          .find(s => s.track && s.track.kind === 'video');
          
        if (sender) {
          await sender.replaceTrack(videoTrack);
        }
        
        // Update local stream
        if (localVideoRef.current) {
          localVideoRef.current.srcObject = stream;
        }
        
        setLocalStream(stream);
        setIsScreenSharing(true);
        
        // Handle when user stops screen sharing using the browser's UI
        stream.getVideoTracks()[0].onended = () => {
          toggleScreenShare();
        };
      }
    } catch (err) {
      console.error('Error toggling screen share:', err);
      setError('Failed to share screen. Please try again.');
    }
  };

  const endCall = () => {
    WebRTCService.cleanup();
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop());
    }
    // Navigate away or show call ended screen
    window.location.href = '/dashboard';
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <Box textAlign="center">
          <CircularProgress />
          <Typography variant="h6" mt={2}>
            Connecting to the call...
          </Typography>
        </Box>
      </Box>
    );
  }

  if (error) {
    return (
      <Box textAlign="center" mt={4}>
        <Typography color="error" variant="h6">
          {error}
        </Typography>
        <Button variant="contained" color="primary" onClick={initializeWebRTC} sx={{ mt: 2 }}>
          Retry
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100vh', p: 2, backgroundColor: '#121212' }}>
      <Grid container spacing={2} sx={{ height: '100%' }}>
        {/* Remote Video */}
        <Grid item xs={12} sx={{ height: '80%', position: 'relative' }}>
          <Paper 
            elevation={3} 
            sx={{ 
              height: '100%', 
              backgroundColor: '#1E1E1E',
              borderRadius: 2,
              overflow: 'hidden',
              position: 'relative',
            }}
          >
            {remoteStream ? (
              <video
                ref={remoteVideoRef}
                autoPlay
                playsInline
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                }}
              />
            ) : (
              <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                height="100%"
                flexDirection="column"
                color="white"
              >
                <Typography variant="h6">Waiting for the other participant to join...</Typography>
                <CircularProgress sx={{ mt: 2 }} />
              </Box>
            )}
          </Paper>
          
          {/* Local Video */}
          {localStream && (
            <Paper 
              elevation={3} 
              sx={{
                position: 'absolute',
                bottom: 20,
                right: 20,
                width: '25%',
                height: '25%',
                minWidth: 200,
                minHeight: 150,
                backgroundColor: '#2D2D2D',
                borderRadius: 1,
                overflow: 'hidden',
                zIndex: 10,
              }}
            >
              <video
                ref={localVideoRef}
                autoPlay
                muted
                playsInline
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                  transform: 'rotateY(180deg)', // Mirror the local video
                }}
              />
            </Paper>
          )}
        </Grid>
        
        {/* Controls */}
        <Grid item xs={12} sx={{ height: '15%' }}>
          <Box 
            display="flex" 
            justifyContent="center" 
            alignItems="center"
            gap={2}
            height="100%"
          >
            <IconButton
              onClick={toggleMute}
              color={isMuted ? 'error' : 'primary'}
              sx={{
                backgroundColor: isMuted ? 'rgba(244, 67, 54, 0.1)' : 'rgba(25, 118, 210, 0.1)',
                '&:hover': {
                  backgroundColor: isMuted ? 'rgba(244, 67, 54, 0.2)' : 'rgba(25, 118, 210, 0.2)',
                },
              }}
            >
              {isMuted ? <MicOff /> : <Mic />}
            </IconButton>
            
            <IconButton
              onClick={toggleVideo}
              color={isVideoOff ? 'error' : 'primary'}
              sx={{
                backgroundColor: isVideoOff ? 'rgba(244, 67, 54, 0.1)' : 'rgba(25, 118, 210, 0.1)',
                '&:hover': {
                  backgroundColor: isVideoOff ? 'rgba(244, 67, 54, 0.2)' : 'rgba(25, 118, 210, 0.2)',
                },
              }}
            >
              {isVideoOff ? <VideocamOff /> : <Videocam />}
            </IconButton>
            
            <IconButton
              onClick={toggleScreenShare}
              color={isScreenSharing ? 'secondary' : 'primary'}
              sx={{
                backgroundColor: isScreenSharing ? 'rgba(156, 39, 176, 0.1)' : 'rgba(25, 118, 210, 0.1)',
                '&:hover': {
                  backgroundColor: isScreenSharing ? 'rgba(156, 39, 176, 0.2)' : 'rgba(25, 118, 210, 0.2)',
                },
              }}
            >
              {isScreenSharing ? <StopScreenShare /> : <ScreenShare />}
            </IconButton>
            
            <IconButton
              onClick={endCall}
              sx={{
                backgroundColor: 'rgba(244, 67, 54, 0.7)',
                color: 'white',
                '&:hover': {
                  backgroundColor: 'rgba(244, 67, 54, 0.9)',
                },
                width: 56,
                height: 56,
              }}
            >
              <CallEnd />
            </IconButton>
          </Box>
          
          {/* Connection status */}
          <Box textAlign="center" mt={1}>
            <Typography variant="caption" color="textSecondary">
              Status: {connectionState} • ICE: {iceConnectionState}
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default VideoCall;
