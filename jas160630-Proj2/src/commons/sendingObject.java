package commons;

import java.io.File;
import java.io.Serializable;

public class sendingObject implements Serializable{
	
	private static final long serialVersionUID = 1L;

	//Common for all
	private String type;
	private Integer fileSize;
	private Integer chunkNo;
	
	//For Client to send Read Information
	private Integer readBytesFrom;
	//For Client to send append Information
	private Integer writeNumOfBytes;
	
	//For MetaServer to send back information to Client
	private String serverName;
	private Integer updatedReadBytesFrom;
	private String chunkName;
	private String message;
	
	//Common for metaserver and client
	private Integer readNumOfBytes;
	private String fileName;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getReadBytesFrom() {
		return readBytesFrom;
	}
	public void setReadBytesFrom(Integer readBytesFrom) {
		this.readBytesFrom = readBytesFrom;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public Integer getUpdatedReadBytesFrom() {
		return updatedReadBytesFrom;
	}
	public void setUpdatedReadBytesFrom(Integer updatedReadBytesFrom) {
		this.updatedReadBytesFrom = updatedReadBytesFrom;
	}
	public String getChunkName() {
		return chunkName;
	}
	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}
	public Integer getReadNumOfBytes() {
		return readNumOfBytes;
	}
	public void setReadNumOfBytes(Integer readNumOfBytes) {
		this.readNumOfBytes = readNumOfBytes;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Integer getWriteNumOfBytes() {
		return writeNumOfBytes;
	}
	public void setWriteNumOfBytes(Integer writeNumOfBytes) {
		this.writeNumOfBytes = writeNumOfBytes;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getFileSize() {
		return fileSize;
	}
	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}
	public Integer getChunkNo() {
		return chunkNo;
	}
	public void setChunkNo(Integer chunkNo) {
		this.chunkNo = chunkNo;
	}	
}
