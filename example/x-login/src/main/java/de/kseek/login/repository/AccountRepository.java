package de.kseek.login.repository;

import de.kseek.login.entity.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储 - MongoDB
 */
@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    /**
     * 按账号查询用户
     */
    Optional<Account> findByAccount(String account);
}
