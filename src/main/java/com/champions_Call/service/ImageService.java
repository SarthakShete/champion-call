package com.champions_Call.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {


  public String uploadImage(MultipartFile contactImage,String fileName);

  public String getUrlFromPublicId(String PublicId);

}
