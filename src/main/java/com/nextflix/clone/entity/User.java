package com.nextflix.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nextflix.clone.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(unique = true)
    private String verificationToken;

    @Column
    private Instant verificationTokenExpiry;

    @Column
    private String passwordResetToken;

    @Column
    private Instant passwordResetTokenExpiry;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_watchlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id")
    )
    private Set<Video> watchlist = new HashSet<>();

    public void addToWatchlist(Video video) {
        watchlist.add(video);
    }

    public void removeFromWatchlist(Video video) {
        watchlist.remove(video);
    }
}


// @JsonIgnore là một annotation trong thư viện Jackson được sử dụng để chỉ định rằng một trường hoặc phương thức nào đó sẽ bị bỏ qua khi
// thực hiện quá trình serialization (chuyển đổi đối tượng Java thành JSON) hoặc deserialization (chuyển đổi JSON thành đối tượng Java).
// Khi một trường được đánh dấu bằng @JsonIgnore, nó sẽ không xuất hiện trong kết quả JSON khi đối tượng được chuyển đổi, và cũng sẽ không được
// sử dụng khi JSON được chuyển đổi trở lại thành đối tượng Java. Điều này thường được sử dụng để bảo vệ thông tin nhạy cảm hoặc để
// tránh vòng lặp vô hạn khi có mối quan hệ giữa các đối tượng.