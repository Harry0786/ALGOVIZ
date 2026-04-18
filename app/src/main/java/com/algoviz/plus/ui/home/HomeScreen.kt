package com.algoviz.plus.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.algoviz.plus.ui.learn.viewmodel.LearnViewModel
import com.algoviz.plus.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch

private data class SheetCardData(
    val backgroundRes: Int,
    val title: String
)

@Composable
fun WelcomeBanner(
    userName: String,
    dateText: String,
    avatarUrl: String? = null,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF1B1B1D).copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34414B)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        placeholder = painterResource(id = com.algoviz.plus.R.drawable.home_screen_icon),
                        error = painterResource(id = com.algoviz.plus.R.drawable.home_screen_icon),
                        fallback = painterResource(id = com.algoviz.plus.R.drawable.home_screen_icon),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = com.algoviz.plus.R.drawable.home_screen_icon),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WELCOME BACK",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hi, $userName",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                onClick = onProfileClick,
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                    color = Color(0xFF111111),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LearningProgressCard(
    progress: Float,
    streakText: String,
    problemText: String
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = com.algoviz.plus.R.drawable.learning_progress_bar),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .padding(horizontal = 28.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LEARNING PROGRESS",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "72%",
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Completed",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.Black.copy(alpha = 0.58f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = streakText,
                    color = Color.White.copy(alpha = 0.42f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = problemText,
                    color = Color.White.copy(alpha = 0.42f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
            }
        }
    }
}

@Composable
fun DashboardSheetCard(
    modifier: Modifier = Modifier,
    backgroundRes: Int,
    title: String
) {
    Box(
        modifier = modifier
            .aspectRatio(1.02f)
            .clip(RoundedCornerShape(34.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(34.dp))
    ) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
        )

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 14.dp),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isRightCard: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.98f),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.algoviz.plus.R.drawable.quick_button),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isRightCard) {
                            scaleX = -1f
                        }
                    },
                contentScale = ContentScale.FillBounds
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F2F2))
                        .border(1.dp, Color(0xFF8E8E8E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(31.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f, fill = true))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ArrowNavButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.28f),
        border = BorderStroke(
            1.dp,
            if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun HomeBottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onLearnClick: () -> Unit,
    onVisualizeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val barShape = RoundedCornerShape(56.dp)

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(100.dp)
            .shadow(
                elevation = 28.dp,
                shape = barShape,
                ambientColor = Color.Black.copy(alpha = 0.85f),
                spotColor = Color.Black.copy(alpha = 0.85f)
            )
            .clip(barShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.algoviz.plus.R.drawable.bottom_nav_bar),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onHomeClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(70.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = com.algoviz.plus.R.drawable.home_screen_icon),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = Color.Black,
                        modifier = Modifier.size(31.dp)
                    )
                }
            }

            Surface(
                onClick = onLearnClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = "Learn",
                    tint = Color(0xFF9EA1A8),
                    modifier = Modifier
                        .size(38.dp)
                        .padding(7.dp)
                )
            }

            Surface(
                onClick = onVisualizeClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "Visualize",
                    tint = Color(0xFF9EA1A8),
                    modifier = Modifier
                        .size(38.dp)
                        .padding(7.dp)
                )
            }

            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF9EA1A8),
                    modifier = Modifier
                        .size(38.dp)
                        .padding(7.dp)
                )
            }
        }
    }
}

@Composable
fun TopicCard(
    icon: ImageVector,
    title: String,
    description: String,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF5EEAD4).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF5EEAD4),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5EEAD4)
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF5EEAD4),
                trackColor = Color.White.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
fun AppLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, com.algoviz.plus.R.drawable.app_logo)

    val painter = remember(drawable) {
        drawable?.let {
            val bitmap = if (it is BitmapDrawable) {
                it.bitmap
            } else {
                val bmp = Bitmap.createBitmap(
                    it.intrinsicWidth.takeIf { w -> w > 0 } ?: 1,
                    it.intrinsicHeight.takeIf { h -> h > 0 } ?: 1,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bmp)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bmp
            }
            BitmapPainter(bitmap.asImageBitmap())
        }
    }

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "AlgoViz Logo",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.25f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onVisualize: () -> Unit = {},
    onLearn: () -> Unit = {},
    onStudyRooms: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    learnViewModel: LearnViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile = profileViewModel.userProfile.collectAsState().value
    val displayName = if (userProfile.username.isNotBlank()) userProfile.username else userProfile.email.substringBefore('@').ifBlank { "AlgoViz User" }
    val scrollState = rememberScrollState()
    val sheetCards = listOf(
        SheetCardData(com.algoviz.plus.R.drawable.bg1, "DSA\n450 SHEETS"),
        SheetCardData(com.algoviz.plus.R.drawable.bg2, "A2Z\nDSA SHEETS"),
        SheetCardData(com.algoviz.plus.R.drawable.bg5, "NEETCODE\nROADMAP")
    )
    val pagerState = rememberPagerState(pageCount = { sheetCards.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.algoviz.plus.R.drawable.bg1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp))
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))

                        WelcomeBanner(
                            userName = displayName,
                            dateText = "Oct 24",
                            avatarUrl = userProfile.avatarUrl,
                            onProfileClick = onProfileClick
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        LearningProgressCard(
                            progress = 0.72f,
                            streakText = "DAY 45 STREAK",
                            problemText = "128/180 PROBLEMS"
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QuickActions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "VIEW ALL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(0.92f),
                                icon = Icons.Outlined.PlayArrow,
                                title = "Visualize",
                                subtitle = "Algorithms",
                                onClick = onVisualize
                            )
                            QuickActionCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(0.92f),
                                icon = Icons.Outlined.Groups,
                                title = "Study",
                                subtitle = "Rooms",
                                isRightCard = true,
                                onClick = onStudyRooms
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(0.92f),
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                title = "Learn",
                                subtitle = "Concepts",
                                onClick = onLearn
                            )
                            QuickActionCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(0.92f),
                                icon = Icons.Outlined.Person,
                                title = "Profile",
                                subtitle = "Settings",
                                isRightCard = true,
                                onClick = onProfileClick
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 42.dp),
                            pageSpacing = 10.dp
                        ) { page ->
                            val card = sheetCards[page]
                            DashboardSheetCard(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                backgroundRes = card.backgroundRes,
                                title = card.title
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArrowNavButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                enabled = pagerState.currentPage > 0,
                                onClick = {
                                    if (pagerState.currentPage > 0) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            ArrowNavButton(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                enabled = pagerState.currentPage < sheetCards.lastIndex,
                                onClick = {
                                    if (pagerState.currentPage < sheetCards.lastIndex) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

            }
        }
    }
}
