package com.im;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 群组Repository
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByCreator(UserEntity creator);
    List<GroupEntity> findByMembersContains(UserEntity user);
    Optional<GroupEntity> findByNameAndCreator(String name, UserEntity creator);
}
