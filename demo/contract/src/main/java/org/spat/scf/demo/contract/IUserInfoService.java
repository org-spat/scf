package org.spat.scf.demo.contract;

import java.util.List;

import org.spat.scf.demo.entity.UserInfo;
import org.spat.scf.protocol.annotation.OperationContract;
import org.spat.scf.protocol.annotation.ServiceContract;


@ServiceContract
public interface IUserInfoService {
	
	@OperationContract
	public void insertUser(UserInfo user) throws Exception;
	
	@OperationContract
	public UserInfo getUserByUserId(long userId) throws Exception;
	
	@OperationContract
	public List<UserInfo> getUserList(int cityId ) throws Exception;
}
