package org.spat.scf.client.utility;

import java.util.Random;

/**
 * 生成隨機數的函數 
 * array為傳入的需要進行隨機設置的數組，
 * limit為隨機數的範圍的上限值
 * num為需要隨機的數的個數
 * 返回值的數組中對應生成的隨機數的下標對應值為1
 * @author Administrator
 *
 */
public class RandomHelper {
	
	public static int randomInt (int limit){
		int result;
		Random random = new Random();
		result = random.nextInt(limit);
		return result;
	}
	
//	num 表示生成的数组中1的个数 在数组中1表示抛弃请求 0表示接受请求
	public static byte[] randomGenerator(int limit, int num) {
		
		byte[] tempArray = new byte[limit];
		
		if (num <= 0) {
			for (int i = 0; i < limit; i++) {
				tempArray[i] = 0;
			}
			return tempArray;
		}
		if (num >= limit) {
			for (int i = 0; i < limit; i++) {
				tempArray[i] = 1;
			}
			return tempArray;
		}
		
		Random random = new Random();
		for (int i = 0; i < num; i++) {
			int temp = Math.abs(random.nextInt())%limit;
			while(tempArray[temp] == 1) {
				temp = Math.abs(random.nextInt())%limit;
			}
			tempArray[temp] = 1;	
		}
		return tempArray;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i <= 10; i++) {
			byte[] array = RandomHelper.randomGenerator(10,i);
			for (int j = 0; j < 10; j++) {
				System.out.print(array[j]);
			}
			System.out.println();
		}

	}
}
