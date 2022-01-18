package com.triceriasolutions.pollbuzz.data.models

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.triceriasolutions.pollbuzz.utils.checkFollowing
import kotlinx.coroutines.tasks.await

class FirestorePagingSource (
    private val db: FirebaseFirestore,
    ): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis() - 604800000)
                .get()
                .await()

            // 604,800,000 = 7 days

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis() - 604800000)
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis() - 604800000)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java).sortedBy {
                    it.creationTime
                },
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

class ExpiredPollsPagingSource (
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java),
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

class FollowingPagingSource (
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis())
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis())
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereGreaterThan("expiryTime", System.currentTimeMillis())
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java).filter {
                    checkFollowing(it.creatorId)
                },
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

class FollowingExpiredPollsPagingSource (
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereLessThan("expiryTime", System.currentTimeMillis())
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java).filter {
                    checkFollowing(it.creatorId)
                },
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}


class MyPollPagingSource (
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java),
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

class VotedPollsPagingSource (
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", FirebaseAuth.getInstance().currentUser!!.uid)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java),
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

class AuthorPollsPagingSource (
    private val author: User,
    private val db: FirebaseFirestore,
): PagingSource<QuerySnapshot, Poll>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Poll>): QuerySnapshot? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Poll> {
        return try {

            val currentPage = params.key ?: db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", author.id)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val firstDocumentSnapshot = currentPage.documents[0]

            val prevPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", author.id)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .endBefore(firstDocumentSnapshot)
                .get()
                .await()

            val nextPage = db.collection("polls")
                .limit(15)
                .whereEqualTo("creatorId", author.id)
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                data = currentPage.toObjects(Poll::class.java),
                prevKey = prevPage,
                nextKey = nextPage
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}