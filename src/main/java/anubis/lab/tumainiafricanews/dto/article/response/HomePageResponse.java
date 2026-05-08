package anubis.lab.tumainiafricanews.dto.article.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomePageResponse {

    private ArticleListHomeResponse featured;
    private List<ArticleListHomeResponse> breaking;
    private List<ArticleListHomeResponse> latest;
    private List<ArticleListHomeResponse> popular;
    private List<CategoryListResponse> categories;

}
