package com.example.uberproject.dto.request;

public class BlockUserRequestDTO {
    private Integer userId;
    private Boolean block;
    private String reason;

    public BlockUserRequestDTO(Integer userId, Boolean block, String reason) {
        this.userId = userId;
        this.block = block;
        this.reason = reason;
    }

    public Integer getUserId() { return userId; }
    public Boolean getBlock() { return block; }
    public String getReason() { return reason; }
}
