/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsb;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author nguyendoanh
 */
public class LSB {
    
    // Hàm padding ( chèn thêm bit)
    public String padding (int b){
        String bin = Integer.toBinaryString(b);
        while(bin.length() < 8){
            bin = "0" + bin;
        }
        return bin;
    }
    
    // Hàm chuyển message sang số nhị phân 
    public String MessagetoBinary(String msg){
        String kq = "";
        for (char c: msg.toCharArray()){
            kq += padding(c);
        }
        // Thêm vào chuỗi secret cho đủ bội số của 3
        while(kq.length() % 3 != 0){
            kq += "0";
        }
        return kq;
    }
    // Hàm chuyển số nhị phân sang kí tự 
    public char BinToChar(String bin_string){
        byte r = 0;
        int n = bin_string.length();
        int i = 0;
        int pow = 1;
        for(char c : bin_string.toCharArray()){
            if(c == '1'){
                i = 0;
                pow = 1;
                while(i < n -1){
                    pow = pow * 2;
                    i++;
                }
                r += pow;
            }
            n--;
        }
        return (char)r;
    }
    // Hàm lấy ra các bit LSB  đã Encode trước đó
    // Hàm toBinaryString(int value) của lớp Integer trong JAVA dùng để lấy chuỗi nhị phân từ biến value
   
    public String getLSBBits(int pixel_rgb){
        Color c = new Color(pixel_rgb);
        String result = "";
        String redInB = Integer.toBinaryString(c.getRed());
        String greenInB = Integer.toBinaryString(c.getGreen());
        String blueInB = Integer.toBinaryString(c.getBlue());
        result += String.valueOf(redInB.charAt(redInB.length() - 1));
        result += String.valueOf(greenInB.charAt(greenInB.length() - 1));
        result += String.valueOf(blueInB.charAt(blueInB.length() - 1 ));
        return result;
    }
    
    public boolean saveImage(BufferedImage buff, String file_type,String file_path){
        try {
            if(file_type.equalsIgnoreCase("PNG")) {
                return ImageIO.write(buff, "PNG", new File(file_path + ".png"));
            }
            return ImageIO.write(buff, "bmp", new File(file_path + ".bmp"));
            
        } catch (IOException ex) {
           System.err.println(ex.getMessage());
        }
        return false;
    }
    
// Hàm ENCODE truyền vào 3 tham số image_path,secret,output_path
    public boolean Encode(String image_path, String secret, String output_path){
        try {
            int msg_lengh = secret.length(); // lấy độ dài của chuỗi secret
            int count = 0;
            // Tạo 1 biến fileName để lưu
            String fileName = "";
            if(output_path.equals("")){
                fileName = image_path.substring(0,image_path.lastIndexOf('.'));
                fileName = "Encode_" + fileName.substring(fileName.lastIndexOf('\\') + 1);
            }
            else{
                fileName = output_path;
            }
            String filetype = image_path.substring(image_path.lastIndexOf('.') + 1);
            String hiddenString = padding(msg_lengh) + MessagetoBinary(secret);
            while(hiddenString.length() %3 != 0){
                hiddenString += "0";
            }
            char[] binHidden = hiddenString.toCharArray();// Chuyển đổi chuỗi secret sang nhị phân (text bits)
            int break_point = binHidden.length; // Lấy ra độ dài của chuỗi secret
            BufferedImage buff = ImageIO.read(new File(image_path)); // Đọc file image vào buff
            for (int x = 0; x < buff.getWidth(); x++){
                for(int y = 0; y < buff.getHeight(); y ++){
                    int rgb = buff.getRGB(x, y); // Lấy ra giá trị màu RGB hiện tại
                    if(binHidden[count++] == '0'){
                        rgb = rgb & 0xFFFEFFFF;
                    }
                    else
                        rgb = rgb | 0x00010000;
                    if(binHidden[count++] == '0'){
                        rgb = rgb & 0xFFFFFEFF;
                    }
                    else
                        rgb = rgb | 0x00000100;
                    if(binHidden[count++] == '0')
                    {
                        rgb = rgb & 0xFFFFFFFE;
                    }
                    else
                        rgb = rgb | 0x00000001;
                    buff.setRGB(x, y, rgb); // Set lại giá trị RGB mới
                    if(count == break_point) break;
                }
                if(count == break_point) break; // Kiểm tra vòng lặp đã encode đủ chuỗi secret chưa
            }
            if(saveImage(buff, filetype, fileName)) // Lưu file đã encode lại
            {
                System.out.println("Đã lưu file ! OK");
                return true;
            }                  
        } 
        catch (IOException ex) {
           System.err.println(ex.getMessage());
        }
        return false;             
    }
   
    public String Decode(String image_path){
        
        try {
            int secret_length = 20;
            int count = 0; // Dùng đếm số bit LSB
            String secret = "";
            BufferedImage buff = ImageIO.read(new File(image_path));
            for(int x = 0; x < buff.getWidth(); x++){
                for(int y = 0; y < buff.getHeight(); y++){
                    secret = secret + getLSBBits(buff.getRGB(x, y));
                    count += 3;
                    if(count == 9){
                        secret_length = Integer.parseInt(secret.substring(0,8),2) + 1;
                        
                    }
                    if(count > (secret_length * 8)) break; // Dừng khi đã đọc hết những hidden message
                }
                if(count > secret_length) break;
            }
            secret = secret.substring(0, secret_length * 8);
            System.out.println(secret);
            String result = ""; // Dùng biến result để lấy ra chuỗi secret
            for (int i = 8 ; i < secret.length(); i+= 8){
                result += BinToChar(secret.substring(i,i+8));
            }
            return result;
        
        } catch (IOException ex) {
             System.err.println(ex.getMessage());
        }
        return "";
    }
     
    public static void main(String[] args) {
        
        
    }
    
}
