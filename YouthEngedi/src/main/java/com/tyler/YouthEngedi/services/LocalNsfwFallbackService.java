package com.tyler.YouthEngedi.services;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.FloatBuffer;
import java.util.Map;

@Service
public class LocalNsfwFallbackService {

    private static final Logger logger = LoggerFactory.getLogger(LocalNsfwFallbackService.class);
    private final OrtEnvironment environment;
    private final OrtSession session;

    public LocalNsfwFallbackService(OrtEnvironment environment, @Value("${onnx.model.path}") Resource modelResource) throws Exception {
        this.environment = environment;

        byte[] modelBytes = modelResource.getInputStream().readAllBytes();
        this.session = environment.createSession(modelBytes, new OrtSession.SessionOptions());
    }

    @PreDestroy
    public void cleanup() throws Exception{
        if(session != null){
            session.close();
        }
    }
    public boolean isSafe(String url){
        try{
            BufferedImage image = ImageIO.read(new URI(url).toURL());

            if(image == null) return false;

            // Resize to model expected shape (usually 224x224)
            BufferedImage resized = new BufferedImage(224,224,BufferedImage.TYPE_INT_RGB);
            resized.createGraphics().drawImage(image,0,0,224,224,null);

            // Preprocess rgb channels into a flat float array (CHW format)
            float[] rgb = new float[3 * 224 * 224];
            int idx = 0;
            for(int c = 0; c < 3;c++){
                for(int y = 0; y < 224;y++){
                    for(int x = 0; x < 224;x++){
                        // Standard channel extraction & baseline normalization
                        int pixel = resized.getRGB(x,y);
                        int val = (c == 0) ? (pixel >> 16) & 0xFF : (c == 1) ? (pixel >> 8) & 0xFF : (pixel & 0xFF);
                        rgb[idx++] = val - 120.0f; //OpenNSFW mean substraction
                    }
                }
            }

            long[] shape = new long[]{1,224,224,3};

            try(OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(rgb),shape);
                OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), tensor))){

                float[][] output = (float[][]) result.get(0).getValue();
                float nsfwScore = output[0][1]; // Index 1 typically represents NSFW score
                logger.debug("Threshold score: {}", nsfwScore);
                return nsfwScore < 0.80f; // Safe if below threshold
            }
        } catch (Exception e) {
            logger.warn("NSFW content validation failed. Flagging image for manual review: {}",e.getMessage(), e);
            return false;
        }
    }
}
