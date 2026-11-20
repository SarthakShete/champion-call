package com.champions_Call.Form.Player;

public record PlayerDetailDTO(Long id,
    String fullName,
    String profileImage,
    String playerIdProof,
    String idType,
    String idNumber,
    boolean verified) {

}
