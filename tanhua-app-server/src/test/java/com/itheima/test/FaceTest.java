package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppServerApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {
    @Autowired
    private AipFaceTemplate template;
    //设置APPID/AK/SK
    public static final String APP_ID = "26963740";
    public static final String API_KEY = "GG8Wic0yX9XOgwfsG1iLozB8";
    public static final String SECRET_KEY = "KfViVVemkvkFoNWLCg6YSE4SoEE2N0Kg";

    public static void main(String[] args) {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);



        // 调用接口
        String image = "https://yishun-tanhua.oss-cn-beijing.aliyuncs.com/2022/08/09/2ff00399-455e-4283-9238-826401b51558.jpg";
        String imageType = "URL";
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        System.out.println(res.toString(2));
        Object error_code = res.get("error_code");
        System.out.println(error_code);
    }
    @Test
    public void detect(){
        boolean detect = template.detect("https://yishun-tanhua.oss-cn-beijing.aliyuncs.com/2022/08/09/2ff00399-455e-4283-9238-826401b51558.jpg");
        System.out.println(detect);
    }

}
