/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.champions_Call.Validators;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class FileValidator implements ConstraintValidator<ValidFile,MultipartFile>{


    private static final long MAX_FILE_SIZE = 1024 * 1024 * 2; // 2MB

    // type  

    //height

    //width


    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {


        if (file == null || file.isEmpty()) {
            return true; 
        }

        // file size...
        if(file.getSize() > MAX_FILE_SIZE){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("file size should be less than 2 MB...").addConstraintViolation();
            return false;
        }

        // file Resolution...
        try{
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            
        } catch (IOException e) {
            e.printStackTrace();

        }
        return true;
    }
}
