package site.minnan.stock.userinterface.fascade;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.minnan.stock.application.service.AuthUserService;
import site.minnan.stock.domain.vo.LoginVO;
import site.minnan.stock.userinterface.dto.LoginDTO;
import site.minnan.stock.userinterface.response.ResponseEntity;

import javax.validation.Valid;

@RestController
@RequestMapping("/stock/auth")
public class AuthController {

    @Autowired
    private AuthUserService authUserService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto){
        LoginVO vo = authUserService.login(dto);
        return ResponseEntity.success(vo);
    }
}
