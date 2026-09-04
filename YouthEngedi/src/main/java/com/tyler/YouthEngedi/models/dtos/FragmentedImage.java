package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FragmentedImage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private MultipartFile chunk;
    private int index;
    private InnerFragmentedImage innerFragmentedImage;
}
