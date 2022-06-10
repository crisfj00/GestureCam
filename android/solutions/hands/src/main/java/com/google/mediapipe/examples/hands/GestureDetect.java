package com.google.mediapipe.examples.hands;


import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GestureDetect {

    // Dictionary mapping icons with gestures
    public static final EnumMap<HandGesture, Integer> gestureEmojis = new EnumMap<>(Map.of(
            HandGesture.VICTORY, 0x270C,
            HandGesture.HORNS, 0x1F918,
            HandGesture.LOVE, 0x1F91F,
            HandGesture.INDEX, 0x261D,
            HandGesture.OK, 0x1f44c,
            HandGesture.MIDDLE, 0x1f595,
            HandGesture.CALL, 0x1F919,
            HandGesture.THUMBS, 0x1F44D,
            HandGesture.FIST, 0x270A
    ));

    public static String handGestureCalculator(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks, HandGesture lastGesture) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand deal";
        }
        boolean thumbIsOpen = false;
        boolean firstFingerIsOpen = false;
        boolean secondFingerIsOpen = false;
        boolean thirdFingerIsOpen = false;
        boolean fourthFingerIsOpen = false;

        //FIXME: something is wrong with the calculation I think
        // Original implementation from github gist, has problems depending on which side of the hand face the camera
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {

            List<LandmarkProto.NormalizedLandmark> landmarkList = landmarks.getLandmarkList();
            float pseudoFixKeyPoint = landmarkList.get(2).getX();
            if (pseudoFixKeyPoint < landmarkList.get(9).getX()) {
                if (landmarkList.get(3).getX() < pseudoFixKeyPoint && landmarkList.get(4).getX() < pseudoFixKeyPoint) {
                    thumbIsOpen = true;
                }
            }
            if (pseudoFixKeyPoint > landmarkList.get(9).getX()) {
                if (landmarkList.get(3).getX() > pseudoFixKeyPoint && landmarkList.get(4).getX() > pseudoFixKeyPoint) {
                    thumbIsOpen = false;
                }
            }
//            Log.d(TAG, "pseudoFixKeyPoint == " + pseudoFixKeyPoint + "\nlandmarkList.get(2).getX() == " + landmarkList.get(2).getX()
//                    + "\nlandmarkList.get(4).getX() = " + landmarkList.get(4).getX());
            pseudoFixKeyPoint = landmarkList.get(6).getY();
            if (landmarkList.get(7).getY() < pseudoFixKeyPoint && landmarkList.get(8).getY() < landmarkList.get(7).getY()) {
                firstFingerIsOpen = true;
            }
            pseudoFixKeyPoint = landmarkList.get(10).getY();
            if (landmarkList.get(11).getY() < pseudoFixKeyPoint && landmarkList.get(12).getY() < landmarkList.get(11).getY()) {
                secondFingerIsOpen = true;
            }
            pseudoFixKeyPoint = landmarkList.get(14).getY();
            if (landmarkList.get(15).getY() < pseudoFixKeyPoint && landmarkList.get(16).getY() < landmarkList.get(15).getY()) {
                thirdFingerIsOpen = true;
            }
            pseudoFixKeyPoint = landmarkList.get(18).getY();
            if (landmarkList.get(19).getY() < pseudoFixKeyPoint && landmarkList.get(20).getY() < landmarkList.get(19).getY()) {
                fourthFingerIsOpen = true;
            }

            // TODO: writing this in a nested fashion might be better, right now reaching "On the Phone" is difficult
            //  The order probably should be 1st -> 2nd -> 3rd -> 4th -> thumb (last because it is the most complex here)
            /* Hand gesture recognition
             * First = Index finger
             *  Second = Middle finger
             *  Third = Ring finger
             *  Fourth = Pinky
             *
             *  All gestures are represented by standard emojis, their strings correspond to the emoji names
             * */
            if (firstFingerIsOpen && secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen && !thumbIsOpen) {
                lastGesture = HandGesture.VICTORY;
                return getEmoji(gestureEmojis.get(HandGesture.VICTORY));
            } else if (firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && fourthFingerIsOpen && !thumbIsOpen) {
                lastGesture = HandGesture.HORNS;
                return getEmoji(gestureEmojis.get(HandGesture.HORNS));
            } else if (thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && fourthFingerIsOpen) {
                lastGesture = HandGesture.LOVE;
                return getEmoji(gestureEmojis.get(HandGesture.LOVE));
            } else if (!fourthFingerIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !thumbIsOpen) {
                lastGesture = HandGesture.INDEX;
                return getEmoji(gestureEmojis.get(HandGesture.INDEX));
            } else if (!firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen && isThumbNearFirstFinger(landmarkList.get(4), landmarkList.get(8))) {
                lastGesture = HandGesture.OK;
                return getEmoji(gestureEmojis.get(HandGesture.OK)); // open fingers have to be stretched
            } else if (!firstFingerIsOpen && secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) { // thumb state doesn't matter
                lastGesture = HandGesture.MIDDLE;
                return getEmoji(gestureEmojis.get(HandGesture.MIDDLE));
            } else if (!firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && fourthFingerIsOpen && thumbIsOpen) {
                lastGesture = HandGesture.CALL;
                return getEmoji(gestureEmojis.get(HandGesture.CALL)); // Barely works

                // This one does not have a fitting emoji
//      } else if (thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
//        return "The L";

            } else if (!fourthFingerIsOpen && thumbIsOpen && !firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && isThumbNearFirstFinger(landmarkList.get(4), landmarkList.get(8))) {
                lastGesture = HandGesture.THUMBS;
                return getEmoji(gestureEmojis.get(HandGesture.THUMBS)); // Barely works
            } else if (!thumbIsOpen && !firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
                lastGesture = HandGesture.FIST;
                return getEmoji(gestureEmojis.get(HandGesture.FIST));
            } else {
                lastGesture = HandGesture.UNDEFINED;
//                String info = "thumbIsOpen " + thumbIsOpen + " firstFingerIsOpen " + firstFingerIsOpen
//                        + " secondFingerIsOpen " + secondFingerIsOpen +
//                        " thirdFingerIsOpen " + thirdFingerIsOpen + " fourthFingerIsOpen " + fourthFingerIsOpen;
//                Log.d(TAG, "handGestureCalculator: == " + info);
                return "unrecognized gesture";
            }
        }
        return "___";
    }

    private static boolean isThumbNearFirstFinger(LandmarkProto.NormalizedLandmark point1, LandmarkProto.NormalizedLandmark point2) {
        double distance = getEuclideanDistanceAB(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        return distance < 0.1;
    }

    private static double getEuclideanDistanceAB(double a_x, double a_y, double b_x, double b_y) {
        double dist = Math.pow(a_x - b_x, 2) + Math.pow(a_y - b_y, 2);
        return Math.sqrt(dist);
    }

    private static String getMultiHandLandmarksDebugString(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        StringBuilder multiHandLandmarksStr = new StringBuilder("Number of hands detected: " + multiHandLandmarks.size() + "\n");
        int handIndex = 0;
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr.append("\t#Hand landmarks for hand[").append(handIndex).append("]: ").append(landmarks.getLandmarkCount()).append("\n");
            int landmarkIndex = 0;
            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                multiHandLandmarksStr.append("\t\tLandmark [").append(landmarkIndex).append("]: (").append(landmark.getX()).append(", ").append(landmark.getY()).append(", ").append(landmark.getZ()).append(")\n");
                ++landmarkIndex;
            }
            ++handIndex;
        }
        return multiHandLandmarksStr.toString();
    }

    // Unicode emoji to String
    public static String getEmoji(int unicode) {
        return new String(Character.toChars(unicode));
    }
}
