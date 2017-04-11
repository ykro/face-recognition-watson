package com.bitandik.labs.facerecognitionwatson.utils;

import android.graphics.RectF;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageFace;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Location;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualRecognitionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ykro.
 */

public class WatsonUtils {
  private static WatsonUtils utils;
  private VisualRecognition service;
  private ArrayList<RectF> facesRectangles;

  public WatsonUtils() {
  }

  public void setService(VisualRecognition service) {
    this.service = service;
  }

  public static WatsonUtils init(String APIKey) {
    if (utils == null) {
      utils = new WatsonUtils();
      VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
      service.setApiKey(APIKey);
      utils.setService(service);

    }
    return utils;
  }


  public String getAnnotations(File file) {
    ClassifyImagesOptions classificationOptions = new ClassifyImagesOptions
        .Builder()
        .images(file)
        .build();

    VisualClassification classificationResult = service
        .classify(classificationOptions)
        .execute();


    VisualRecognitionOptions recognitionOptions = new VisualRecognitionOptions
        .Builder()
        .images(file)
        .build();


    DetectedFaces recognitionResult = service
        .detectFaces(recognitionOptions)
        .execute();

    return processResponse(classificationResult, recognitionResult);
  }

  public String processResponse(VisualClassification classification, DetectedFaces faces) {
    facesRectangles = new ArrayList<RectF>();
    String message = "I found these things:\n\n";
    List<ImageClassification> images = classification.getImages();
    if (images != null) {
      for (ImageClassification imgClassification : images) {
        List<VisualClassifier> classifiers = imgClassification.getClassifiers();
        if (classifiers != null) {
          for (VisualClassifier classifier : classifiers){
            List<VisualClassifier.VisualClass> classes = classifier.getClasses();
            for (VisualClassifier.VisualClass visualClass : classes) {
              message += String.format(Locale.US, "%.3f: %s\n", visualClass.getScore(), visualClass.getName());
            }
          }
        }
      }
    }

    List<ImageFace> facesImages = faces.getImages();
    if (facesImages != null) {
      for (ImageFace imgFace : facesImages) {
        List<Face> detectedFaces = imgFace.getFaces();
        if (detectedFaces != null) {
          for (Face face : detectedFaces){
            Face.Age age = face.getAge();
            if (age != null && age.getScore() > 0.35f){
              message += String.format(Locale.US, "%.3f Min:%d Max:%d\n", age.getScore(), age.getMin(), age.getMax());
            }

            Face.Gender gender = face.getGender();
            if (gender != null && gender.getScore() > 0.0f) {
              message += String.format(Locale.US, "%.3f: Gender %s\n", gender.getScore(), gender.getGender());
            }

            Face.Identity identity = face.getIdentity();
            if (identity != null) {
              message += String.format(Locale.US, "%.3f: %s\n", identity.getScore(), identity.getName());
            }

            Location location = face.getLocation();
            if (location != null) {

              RectF faceRectangle = new RectF(location.getLeft(),
                                              location.getTop(),
                                              location.getLeft() + location.getWidth(),
                                              location.getTop() + location.getHeight());
              facesRectangles.add(faceRectangle);
            }

          }
        }
      }
    }

    return message;
  }

  public ArrayList<RectF> getFacesRectangles() {
    return this.facesRectangles;
  }
}
