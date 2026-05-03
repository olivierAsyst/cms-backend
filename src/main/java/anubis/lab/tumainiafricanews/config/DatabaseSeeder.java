package anubis.lab.tumainiafricanews.config;

import anubis.lab.tumainiafricanews.entity.Article;
import anubis.lab.tumainiafricanews.entity.Category;
import anubis.lab.tumainiafricanews.entity.Tag;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import anubis.lab.tumainiafricanews.repository.ArticleRepository;
import anubis.lab.tumainiafricanews.repository.CategoryRepository;
import anubis.lab.tumainiafricanews.repository.TagRepository;
import anubis.lab.tumainiafricanews.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Profile("!prod")
@Slf4j
public class DatabaseSeeder {//implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;

    private final List<User> authors = new ArrayList<>();
    private final Random random = new Random();

    //@Override
    public void run(String... args) throws Exception {
        if (articleRepository.count() > 20) {
            log.info("✅ Base de données déjà seedée.");
            return;
        }

        log.info("🚀 Début du seeding réaliste...");

        seedUsers();
        seedCategories();
        seedTags();
        seedArticles(65);   // 65 articles réalistes

        log.info("✅ Seeding terminé avec 65 articles réalistes !");
    }

    private void seedUsers() {
        authors.addAll(userRepository.findAll());
        if (authors.isEmpty()) {
            log.error("Aucun utilisateur trouvé pour le seeding !");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> cats = List.of(
                createCat("Politique", "politique"),
                createCat("Économie", "economie"),
                createCat("Société", "societe"),
                createCat("Sport", "sport"),
                createCat("Technologie", "technologie"),
                createCat("Santé", "sante"),
                createCat("Culture & Art", "culture"),
                createCat("Environnement", "environnement")
        );
        categoryRepository.saveAll(cats);
    }

    private Category createCat(String name, String slug) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription("Actualités " + name.toLowerCase() + " au Burundi et en Afrique");
        return c;
    }

    private void seedTags() {
        if (tagRepository.count() > 0) return;
        List<String> names = List.of("Burundi", "EAC", "CNDD-FDD", "Jeunesse", "Agriculture",
                "Climat", "IA", "Football", "Corruption", "Tourisme", "Éducation", "Santé");

        List<Tag> tags = names.stream().map(name -> {
            Tag t = new Tag();
            t.setName(name);
            t.setSlug(name.toLowerCase());
            return t;
        }).toList();
        tagRepository.saveAll(tags);
    }

    private void seedArticles(int count) {
        List<Category> categories = categoryRepository.findAll();
        List<Tag> allTags = tagRepository.findAll();

        List<Article> articles = new ArrayList<>();

        // Titres réalistes
        String[] titles = {
                "Le Burundi adopte sa stratégie nationale d'intelligence artificielle 2026-2030",
                "Élections législatives : le CNDD-FDD remporte une victoire écrasante",
                "L'agriculture burundaise menacée par les changements climatiques",
                "Victoire historique des Intamba du Burundi en Coupe d'Afrique",
                "Inflation : le gouvernement annonce un plan de stabilisation économique",
                "Gitega : la nouvelle capitale politique attire investisseurs et touristes",
                "Jeunes entrepreneurs burundais : succès dans le numérique et l'agro-tech",
                "Partenariat EAC : le Burundi renforce ses échanges avec le Kenya et la Tanzanie",
                "Campagne nationale de vaccination contre la rougeole lancée à Bujumbura",
                "Corruption : une nouvelle affaire secoue le ministère des Infrastructures"
        };

        for (int i = 0; i < count; i++) {
            Article a = new Article();

            a.setTitle(titles[i % titles.length] + " - Vol. " + (2026 + i % 3));
            a.setSlug("burundi-" + (i+1) + "-" + UUID.randomUUID().toString().substring(0,6));

            a.setSummary(generateRealisticSummary(i));
            a.setContent(generateRealisticContent(i));   // Contenu plus long

            a.setAuthor(authors.get(random.nextInt(authors.size())));
            a.setCategory(categories.get(random.nextInt(categories.size())));

            // Tags (3 à 5)
            Set<Tag> selectedTags = new HashSet<>();
            for (int j = 0; j < random.nextInt(3) + 3; j++) {
                selectedTags.add(allTags.get(random.nextInt(allTags.size())));
            }
            a.setTags(selectedTags);

            // Images variées
            int imageId = 100 + (i % 80); // IDs différents
            a.setMainImageUrl("https://picsum.photos/id/" + imageId + "/1200/800");

            // Statuts variés
            ArticleStatus status = switch (i % 5) {
                case 0, 1 -> ArticleStatus.PUBLISHED;
                case 2 -> ArticleStatus.DRAFT;
                case 3 -> ArticleStatus.PENDING;
                default -> ArticleStatus.PUBLISHED;
            };
            a.setStatus(status);

            if (status == ArticleStatus.PUBLISHED) {
                a.setPublishedAt(LocalDateTime.now().minusDays(random.nextInt(90)));
            }

            a.setFeatured(i % 7 == 0);
            a.setBreaking(i % 12 == 0);
            a.setViewCount(random.nextInt(25000) + 120);

            articles.add(a);
        }

        articleRepository.saveAll(articles);
    }

    private String generateRealisticSummary(int i) {
        return "Dans un contexte marqué par les réformes engagées par le gouvernement, cet article analyse les enjeux et perspectives de " +
                (i % 3 == 0 ? "la transformation numérique" : "la stabilité politique et économique") + " au Burundi.";
    }

    private String generateRealisticContent(int i) {
        return """
            <p>Le Burundi continue sa marche vers le développement durable malgré les défis économiques et climatiques. 
            Selon les experts, l'adoption des nouvelles technologies pourrait permettre au pays de créer plus de 50 000 emplois d'ici 2030.</p>
            
            <p>Le Président de la République a inauguré ce lundi un nouveau centre de formation en intelligence artificielle à Gitega. 
            Cette initiative s'inscrit dans la nouvelle stratégie nationale du numérique 2026-2030.</p>
            
            <p>Les acteurs de la société civile saluent cette avancée tout en appelant à une plus grande inclusion des jeunes et des femmes dans ces projets.</p>
            
            <h3>Des défis persistent</h3>
            <p>Malgré ces avancées, le chômage des jeunes reste élevé et l'accès à l'électricité demeure un frein majeur au déploiement des technologies modernes.</p>
            """;
    }
}