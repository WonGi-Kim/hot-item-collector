package com.sparta.hotitemcollector.domain.user;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sparta.hotitemcollector.TestConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
public class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Test
	@DisplayName("1. findByNicknameContainingIgnoreCase를 테스트한다")
	void Test1() {
		// given
		String nickname = "id";

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		List<User> userList = userRepository.findByNicknameContainingIgnoreCase(nickname);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");

		for (User user : userList) {
			System.out.println("User ID: " + user.getId());
			System.out.println("User Nickname: " + user.getNickname());
			//	System.out.println("User Profile Image: " + user.getProfileImage().getImageUrl());
			System.out.println("------------------------------------");
		}
	}

	/*@Test
	@DisplayName("2. existsByNicknameContainingIgnoreCase를 테스트한다")
	void Test2() {
		// given
		String nickname = "dssds";

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		Boolean isExist = userRepository.existsByNicknameContainingIgnoreCase(nickname);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");

		System.out.println(isExist);
	}*/
}
