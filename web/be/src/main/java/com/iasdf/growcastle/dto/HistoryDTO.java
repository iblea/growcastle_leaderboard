package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.iasdf.growcastle.domain.HistoryPlayer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(title = "HistoryDTO", description = "History DTO")
public class HistoryDTO
{
    @Schema(description = "성공 여부", example = "true")
    private Boolean success;
    // @Schema(description = "해당 데이터가 파싱된 시각", example = "2021-09-01T00:00:00")
    // private LocalDateTime parseTime;
    @Schema(description = "데이터 개수", example = "10")
    private int cnt;
    @Schema(description = "History Player / Guild Data")
    private List<HistoryPlayerDTO> data;


    public HistoryDTO() {
        this.success = false;
        this.cnt = 0;
        // this.parseTime = LocalDateTime.now();
        this.data = null;
    }

    public HistoryDTO(List<HistoryPlayerDTO> data) {
        this.success = true;
        // this.parseTime = LocalDateTime.now();
        this.data = data;
        if (this.data == null) {
            this.success = false;
            this.cnt = 0;
        } else {
            this.cnt = this.data.size();
        }
    }

    public static HistoryDTO toDTO(List<HistoryPlayer> data) {
        if (data == null) {
            return null;
        }

        if (data.isEmpty()) {
            return new HistoryDTO(new LinkedList<>());
        }

        List<HistoryPlayerDTO> result = HistoryPlayerDTO.toDTO(data);
        if (result == null) {
            return null;
        }
        // LocalDateTime time = data.get(0).getMemberPK().getParseTime();
        return new HistoryDTO(result);
    }

    public static HistoryDTO toDTO(HistoryPlayer data) {
        if (data == null) {
            return null;
        }

        HistoryPlayerDTO result = HistoryPlayerDTO.toDTO(data);
        if (result == null) {
            return null;
        }
        List<HistoryPlayerDTO> list = new LinkedList<>();
        list.add(result);
        return new HistoryDTO(list);
    }

}