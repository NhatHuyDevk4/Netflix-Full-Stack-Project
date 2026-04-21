package com.nextflix.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "videos")
@Getter
@Setter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    private Integer year;
    private String rating;
    private Integer duration; // Duration in minutes

    @Column(name = "src")
    @JsonIgnore
    private String srcUuid; // URL or path to the video file

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "poster")
    @JsonIgnore
    private String posterUuid; // URL or path to the poster image

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_categories", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Transient
    @JsonProperty("isInWatchlist")
    private Boolean isInWatchlist; // Indicates if the video is in the user's watchlist

    @JsonProperty("src")
    public String getSrc() {
        // Logic to convert srcUuid to an actual URL or path can be implemented here
        if(srcUuid == null && !srcUuid.isEmpty()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toString();
            return baseUrl+ "/api/files/videos/" + srcUuid;
        }
        return null;
    }

    @JsonProperty("poster")
    public String getPoster() {
    if(posterUuid != null && !posterUuid.isEmpty()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toString();
            return baseUrl+ "/api/files/image/" + posterUuid;
        }
        return null;
    }

}


// Transient là một annotation trong Java Persistence API (JPA) được sử dụng
// để đánh dấu một trường (field) trong một entity class là không được ánh xạ vào cơ sở dữ liệu. Khi một trường được đánh dấu là @Transient
// , nó sẽ không được lưu trữ trong bảng của cơ sở dữ liệu và cũng sẽ không được truy vấn khi lấy dữ liệu từ cơ sở dữ liệu.

// JsonIgnore là một annotation trong thư viện Jackson được sử dụng để chỉ định rằng một trường hoặc phương thức nào đó sẽ bị bỏ qua khi
// thực hiện quá trình serialization (chuyển đổi đối tượng Java thành JSON) hoặc deserialization
// (chuyển đổi JSON thành đối tượng Java). Khi một trường được đánh dấu bằng @JsonIgnore, nó sẽ không xuất hiện
// trong kết quả JSON khi đối tượng được chuyển đổi, và cũng sẽ không được sử dụng khi JSON được chuyển đổi trở lại
// thành đối tượng Java. Điều này thường được sử dụng để bảo vệ thông tin nhạy cảm hoặc để loại bỏ các trường không cần thiết khỏi kết quả JSON.

// JsonProperty là một annotation trong thư viện Jackson được sử dụng để chỉ định tên của trường khi thực hiện quá trình serialization (chuyển đổi đối tượng Java thành JSON) hoặc deserialization (chuyển đổi JSON thành đối tượng Java). Khi một trường được đánh dấu bằng @JsonProperty, tên của trường trong JSON sẽ được xác định bởi giá trị của annotation này thay vì tên của trường trong lớp Java.
// Điều này hữu ích khi bạn muốn ánh xạ một trường Java có tên khác với tên trường trong
// JSON hoặc khi bạn muốn tuân thủ một quy ước đặt tên cụ thể trong JSON.