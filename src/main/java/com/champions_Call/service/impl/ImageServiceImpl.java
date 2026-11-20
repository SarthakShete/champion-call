package com.champions_Call.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Helper.AppConstants;
import com.champions_Call.service.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ImageServiceImpl implements ImageService{

  @Autowired
  private Cloudinary cloudinary;

  @Autowired
  public ImageServiceImpl(Cloudinary cloudinary){
    this.cloudinary = cloudinary;
  }

  @Override
  public String uploadImage(MultipartFile contactImage, String fileName) {

      if(contactImage==null || contactImage.isEmpty()){
        System.out.println("No Image Recieved");
        return null;
      }
      try{
      // ✅ Correct way to read file
        Map uploadResult = cloudinary.uploader().upload(
                contactImage.getBytes(),
                ObjectUtils.asMap("public_id", fileName)
        );

        String imageUrl = uploadResult.get("secure_url").toString();

        System.out.println("Image URL to be stored in DB: " + imageUrl); 
        // ✅ Use Cloudinary returned URL
       // return uploadResult.get("secure_url").toString();

       return imageUrl;

      } catch(Exception e){
        e.printStackTrace();
        return null;
      }   

  }

  @Override
  public String getUrlFromPublicId(String PublicId) {
   return cloudinary
      .url()
      .transformation(
        new Transformation<>()
        .width(AppConstants.CONTACT_IMAGE_WIDTH)
        .height(AppConstants.CONTACT_IMAGE_HEIGHT)
        .crop(AppConstants.CONTACT_IMAGE_CROP)
      )
      .generate(PublicId);
  }

}
