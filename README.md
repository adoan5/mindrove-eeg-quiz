# MindRove EEG Quiz

MindRove EEG Quiz is an interactive Brain-Computer Interface (BCI) quiz game that requires a MindRove Arc device (MindRove Arc) to operate.

To begin, connect to the MindRove Arc via WiFi. Once connected, you'll see real-time EEG data displayed on your screen. This data is processed using a third-order, high-pass Butterworth filter and is normalized with the running absolute average of the collected data. Please note that no data is stored; it is used exclusively during your active session.

The primary feature of the app is a quiz game that you control with the MindRove Arc. By tilting your head left or right, you can navigate through the potential answers. Clenching your teeth selects an option. After answering 10 questions, your final score is displayed.

The app uses a Shallow ConvNet Neural Network to detect clenching actions. You can fine-tune this detection through a setup option in the app. During the setup, you'll be guided to tilt your head left and right to calibrate the detection of head movements. You'll then go through a series of sessions where you’ll clench your teeth or rest when prompted by an on-screen symbol. The neural network is adjusted based on the data collected during these sessions, enhancing its ability to accurately classify your head tilts and clenching actions based on your specific use of the headset.

