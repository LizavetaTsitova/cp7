package cp7.services;

import cp7.entities.Companies;
import cp7.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private MailSender mailSender;

    public boolean registerCompany(Companies company) {
        if (companyRepository.findByEmail(company.getEmail()) != null) {
            return false;
        }

        company.setStatus(false); // Устанавливаем статус 0 (не подтверждено)
        company.setActivationCode(UUID.randomUUID().toString());

        companyRepository.save(company);

        String message = String.format(
                "Добро пожаловать, %s! " +
                        "Пожалуйста, подтвердите регистрацию, перейдя по ссылке: http://localhost:8080/activation/%s",
                company.getName(),
                company.getActivationCode()
        );
        mailSender.sendConfirmationEmail(company.getEmail(),"Company confirmation", message);
        return true;
    }

    public boolean activateCompany(String code) {
        Companies company = companyRepository.findByActivationCode(code);

        if(company.getStatus() == true){
            return false;
        }
        company.setStatus(true);
        companyRepository.save(company);

        return true;
    }
}
