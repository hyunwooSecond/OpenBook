package Project.OpenBook.Service;

import Project.OpenBook.Constants.Role;
import Project.OpenBook.Domain.Admin;
import Project.OpenBook.Repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final AuthenticationManagerBuilder authenticationManager;
    private final PasswordEncoder passwordEncoder;


    /**
     * 관리자 회원가입 메서드
     * @param loginId -> 로그인아이디
     * @param password -> 비밀번호
     * @return 이미 가입이 되어있는 아이디로 회원가입을 시도할경우 null 리턴
     * @return 정상적인 경우는 가입된 Admin정보 리턴
     */

    public Admin registerAdmin(String loginId, String password) {
        Optional<Admin> optionalAdmin = adminRepository.findByLoginId(loginId);
        //중복된 아이디로 회원가입을 시도할경우
        if(optionalAdmin.isPresent()) return null;
        password = passwordEncoder.encode(password);
        Admin admin = Admin.builder()
                .loginId(loginId)
                .password(password)
                .role(Role.ADMIN)
                .build();
        adminRepository.save(admin);
        return admin;
    }

    public Admin login(String loginId, String password){
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(loginId, password);
        Authentication authentication = authenticationManager.getObject().authenticate(upToken);

        if(!authentication.isAuthenticated()){
            return null;
        }
            Optional<Admin> adminOptional = adminRepository.findByLoginId(loginId);
            if (adminOptional.isEmpty()) {
                return null;
            }
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        Admin admin = adminOptional.get();
        return admin;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByLoginId(username).orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 관리자입니다."));
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities(admin.getAuthorities())
                .build();
    }
}
