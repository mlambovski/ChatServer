/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.Serializable;

/**
 *
 * @author Myro
 */
public class FileInfo implements Serializable {

    private byte[] byteArr;
    private String fileName;

    public FileInfo(byte[] byteArr, String fileName) {
        setByteArr(byteArr);
        this.fileName = fileName;
    }

    public byte[] getByteArr() {
        return byteArr;
    }

    public void setByteArr(byte[] byteArr) {
        if (byteArr != null) {
            this.byteArr = new byte[byteArr.length];
            for (int i = 0; i < byteArr.length; i++) {
                this.byteArr[i] = byteArr[i];
            }
        } else {
            this.byteArr = new byte[0];
        }
    }

    public String getFileName() {
        return fileName;
    }
}
