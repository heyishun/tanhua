package com.itheima.test;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class OSSTest {
    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void Test() throws FileNotFoundException {
        String path="C:\\Users\\HYS\\Pictures\\04.jpg";
        FileInputStream inputStream=new FileInputStream(new File(path));
        String filename=new SimpleDateFormat("yyyy/MM/dd").format(new Date())+"/"+ UUID.randomUUID().toString()+path.substring(path.lastIndexOf("."));
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tM8a2LEqpGRsps79VTo";
        String accessKeySecret = "ntadAokrqIJP94eGbgIkkjpFZs8VER";



        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 填写Byte数组。
            byte[] content = "Hello OSS".getBytes();
            // 创建PutObject请求。
            ossClient.putObject("yishun-tanhua", filename,inputStream);
            ossClient.shutdown();
            String url="https://yishun-tanhua.oss-cn-beijing.aliyuncs.com/"+filename;
        System.out.println(url);
        }
        @Test
    public void testTemplate() throws FileNotFoundException {
        String path="C:\\Users\\HYS\\Pictures\\04.jpg";
        FileInputStream inputStream=new FileInputStream(new File(path));
            String imageUrl = ossTemplate.upload(path, inputStream);
            System.out.println(imageUrl);
        }

        }





